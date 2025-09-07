package com.metronom.app

import android.content.Context
import android.media.*
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

class MetronomeEngine(private val context: Context? = null) {
    
    enum class Instrument {
        GUITAR, BASS, DRUMS, CUSTOM
    }
    
    enum class TimeSignature(val numerator: Int, val denominator: Int) {
        FOUR_FOUR(4, 4),
        THREE_FOUR(3, 4),
        TWO_FOUR(2, 4)
    }
    
    // Beat callback interface for precise timing
    interface BeatCallback {
        fun onBeat(beatNumber: Int)
    }
    
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var _tempo = 120
    private var latency = 50L // milliseconds
    private var instrument = Instrument.GUITAR
    private var timeSignature = TimeSignature.FOUR_FOUR
    private var metronomeJob: Job? = null
    private var currentBeat = 1
    private var customSoundData: ByteArray? = null
    private var customSoundDuration = 0.1f // seconds
    private var beatCallback: BeatCallback? = null
    
    // Audio parameters
    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    
    // Sound generation parameters
    private val clickDuration = 0.1f // seconds
    private val clickFrequency = 1000f // Hz
    
    fun setTempo(newTempo: Int) {
        _tempo = newTempo.coerceIn(40, 200)
    }
    
    fun getTempo(): Int = _tempo
    
    fun setLatency(newLatency: Long) {
        latency = newLatency.coerceIn(0, 500)
    }
    
    fun setInstrument(newInstrument: Instrument) {
        instrument = newInstrument
    }
    
    fun setTimeSignature(newTimeSignature: TimeSignature) {
        timeSignature = newTimeSignature
        currentBeat = 1
    }
    
    fun loadCustomSound(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                customSoundData = file.readBytes()
                customSoundDuration = 0.1f // Default duration, could be calculated from file
                instrument = Instrument.CUSTOM
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun clearCustomSound() {
        customSoundData = null
        instrument = Instrument.GUITAR
    }
    
    fun getCurrentBeat(): Int = currentBeat
    
    fun getTimeSignature(): TimeSignature = timeSignature
    
    fun getInstrument(): Instrument = instrument
    
    fun setBeatCallback(callback: BeatCallback?) {
        beatCallback = callback
    }
    
    fun start() {
        if (isPlaying) return
        
        isPlaying = true
        initializeAudioTrack()
        startMetronomeLoop()
    }
    
    fun stop() {
        isPlaying = false
        metronomeJob?.cancel()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
    
    private fun initializeAudioTrack() {
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        
        audioTrack?.play()
    }
    
    private fun startMetronomeLoop() {
        metronomeJob = CoroutineScope(Dispatchers.IO).launch {
            // Use more precise timing with nanoseconds
            val intervalNs = (60000000000.0 / _tempo).toLong() // Convert to nanoseconds
            var nextBeatTime = System.nanoTime()
            
            while (isPlaying) {
                val currentTime = System.nanoTime()
                
                // Wait until it's time for the next beat
                if (currentTime < nextBeatTime) {
                    val waitTime = (nextBeatTime - currentTime) / 1_000_000 // Convert to milliseconds
                    delay(waitTime.coerceAtLeast(1)) // Minimum 1ms delay
                    continue
                }
                
                // Generate and play click sound
                generateClickSound()
                
                currentBeat++
                if (currentBeat > timeSignature.numerator) {
                    currentBeat = 1
                }
                
                // Trigger beat callback immediately for precise visual sync
                beatCallback?.onBeat(currentBeat)
                
                // Calculate next beat time with high precision
                nextBeatTime += intervalNs
            }
        }
    }
    
    private fun generateClickSound() {
        if (instrument == Instrument.CUSTOM && customSoundData != null) {
            // Play custom sound
            audioTrack?.write(customSoundData!!, 0, customSoundData!!.size)
            return
        }
        
        val samples = (sampleRate * clickDuration).toInt()
        val buffer = ByteBuffer.allocate(samples * 2) // 16-bit samples
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        
        val frequency = when (instrument) {
            Instrument.GUITAR -> 1000f
            Instrument.BASS -> 800f
            Instrument.DRUMS -> 1200f
            Instrument.CUSTOM -> 1000f // Fallback
        }
        
        // Add accent for first beat
        val isAccent = currentBeat == 1
        val accentMultiplier = if (isAccent) 1.5f else 1.0f
        
        for (i in 0 until samples) {
            val time = i.toFloat() / sampleRate
            val amplitude = when (instrument) {
                Instrument.GUITAR -> generateGuitarClick(time, frequency, isAccent)
                Instrument.BASS -> generateBassClick(time, frequency, isAccent)
                Instrument.DRUMS -> generateDrumClick(time, frequency, isAccent)
                Instrument.CUSTOM -> 0f
            }
            
            val sample = (amplitude * accentMultiplier * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            buffer.putShort(sample.toShort())
        }
        
        audioTrack?.write(buffer.array(), 0, buffer.array().size)
    }
    
    private fun generateGuitarClick(time: Float, frequency: Float, isAccent: Boolean): Float {
        // Sharp attack with quick decay for guitar
        val envelope = exp(-time * 15f).toFloat()
        val wave = sin(2 * PI * frequency * time).toFloat()
        val baseAmplitude = if (isAccent) 0.4f else 0.3f
        return envelope * wave * baseAmplitude
    }
    
    private fun generateBassClick(time: Float, frequency: Float, isAccent: Boolean): Float {
        // Deeper, more sustained sound for bass
        val envelope = exp(-time * 8f).toFloat()
        val wave = sin(2 * PI * frequency * time).toFloat()
        val baseAmplitude = if (isAccent) 0.5f else 0.4f
        return envelope * wave * baseAmplitude
    }
    
    private fun generateDrumClick(time: Float, frequency: Float, isAccent: Boolean): Float {
        // Sharp, percussive sound for drums
        val envelope = exp(-time * 25f).toFloat()
        val wave = sin(2 * PI * frequency * time).toFloat()
        val noise = (Math.random() * 2 - 1).toFloat() * 0.1f
        val baseAmplitude = if (isAccent) 0.6f else 0.5f
        return envelope * (wave + noise) * baseAmplitude
    }
    
    fun isCurrentlyPlaying(): Boolean = isPlaying
}
