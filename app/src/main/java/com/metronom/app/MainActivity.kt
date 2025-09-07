package com.metronom.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.metronom.app.databinding.ActivityMainBinding
import com.metronom.app.SimpleSongManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var metronomeEngine: MetronomeEngine
    private lateinit var songManager: SimpleSongManager
    private var isPlaying = false
    private var metronomeAnimation: ObjectAnimator? = null
    private var beatUpdateJob: Job? = null
    
    // Latency presets
    private val speakersLatency = 50L
    private val headphonesLatency = 20L
    
    // File picker request code
    private val PICK_AUDIO_FILE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        songManager = SimpleSongManager(this)
        metronomeEngine = MetronomeEngine(this)
        setupUI()
    }
    
    private fun setupUI() {
        // Tempo slider
        binding.tempoSlider.addOnChangeListener { _, value, _ ->
            val tempo = value.toInt()
            binding.tempoValue.text = tempo.toString()
            metronomeEngine.setTempo(tempo)
            binding.beatVisualizer.updateTempo(tempo)
        }
        
        // Latency slider
        binding.latencySlider.addOnChangeListener { _, value, _ ->
            val latency = value.toLong()
            binding.latencyValue.text = "${latency}ms"
            metronomeEngine.setLatency(latency)
        }
        
        // Latency preset buttons
        binding.speakersButton.setOnClickListener {
            setLatencyPreset(speakersLatency)
            updateLatencyButtonStates(true, false)
        }
        
        binding.headphonesButton.setOnClickListener {
            setLatencyPreset(headphonesLatency)
            updateLatencyButtonStates(false, true)
        }
        
        // Start/Stop button
        binding.startStopButton.setOnClickListener {
            if (isPlaying) {
                stopMetronome()
            } else {
                startMetronome()
            }
        }
        
        // Sound selection buttons
        binding.clickButton.setOnClickListener {
            metronomeEngine.setInstrument(MetronomeEngine.Instrument.GUITAR)
            updateSoundButtonStates(true, false, false)
        }
        
        binding.beepButton.setOnClickListener {
            metronomeEngine.setInstrument(MetronomeEngine.Instrument.BASS)
            updateSoundButtonStates(false, true, false)
        }
        
        binding.tickButton.setOnClickListener {
            metronomeEngine.setInstrument(MetronomeEngine.Instrument.DRUMS)
            updateSoundButtonStates(false, false, true)
        }
        
        // Custom sound buttons
        binding.uploadSoundButton.setOnClickListener {
            openFilePicker()
        }
        
        binding.clearCustomSoundButton.setOnClickListener {
            metronomeEngine.clearCustomSound()
            updateCustomSoundStatus()
        }
        
        // Song library button
        binding.songLibraryButton.setOnClickListener {
            val intent = Intent(this, SongListActivity::class.java)
            startActivityForResult(intent, 1001)
        }
        
        // Initialize with default values
        setLatencyPreset(speakersLatency)
        updateLatencyButtonStates(true, false)
        updateSoundButtonStates(true, false, false)
        updateCustomSoundStatus()
    }
    
    private fun setLatencyPreset(latency: Long) {
        binding.latencySlider.value = latency.toFloat()
        binding.latencyValue.text = "${latency}ms"
        metronomeEngine.setLatency(latency)
    }
    
    private fun updateLatencyButtonStates(speakersSelected: Boolean, headphonesSelected: Boolean) {
        binding.speakersButton.isSelected = speakersSelected
        binding.headphonesButton.isSelected = headphonesSelected
        
        if (speakersSelected) {
            binding.speakersButton.setBackgroundColor(getColor(R.color.blue_accent))
            binding.headphonesButton.setBackgroundColor(getColor(R.color.white_alpha_30))
        } else {
            binding.headphonesButton.setBackgroundColor(getColor(R.color.blue_accent))
            binding.speakersButton.setBackgroundColor(getColor(R.color.white_alpha_30))
        }
    }
    
    private fun updateSoundButtonStates(clickSelected: Boolean, beepSelected: Boolean, tickSelected: Boolean) {
        binding.clickButton.isSelected = clickSelected
        binding.beepButton.isSelected = beepSelected
        binding.tickButton.isSelected = tickSelected
        
        val selectedColor = getColor(R.color.purple_accent)
        val unselectedColor = getColor(R.color.white_alpha_30)
        
        binding.clickButton.setBackgroundColor(if (clickSelected) selectedColor else unselectedColor)
        binding.beepButton.setBackgroundColor(if (beepSelected) selectedColor else unselectedColor)
        binding.tickButton.setBackgroundColor(if (tickSelected) selectedColor else unselectedColor)
    }
    
    private fun startMetronome() {
        isPlaying = true
        binding.startStopButton.text = getString(R.string.stop)
        
        // Set up beat callback for precise timing
        metronomeEngine.setBeatCallback(object : MetronomeEngine.BeatCallback {
            override fun onBeat(beatNumber: Int) {
                runOnUiThread {
                    binding.beatCounter.text = beatNumber.toString()
                    binding.beatVisualizer.onBeat()
                }
            }
        })
        
        metronomeEngine.start()
        binding.beatVisualizer.startBeatAnimation(metronomeEngine.getTempo())
    }
    
    private fun stopMetronome() {
        isPlaying = false
        binding.startStopButton.text = getString(R.string.start)
        
        // Clear beat callback
        metronomeEngine.setBeatCallback(null)
        
        metronomeEngine.stop()
        binding.beatVisualizer.stopBeatAnimation()
    }
    
    // Removed old animation methods - now handled by BeatVisualizer directly
    
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Audio File"), PICK_AUDIO_FILE)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_AUDIO_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                loadCustomSound(uri)
            }
        } else if (requestCode == 1001 && resultCode == SongListActivity.RESULT_SONG_LOADED) {
            // Song was loaded from SongListActivity
            data?.let { intent ->
                val songName = intent.getStringExtra("song_name") ?: ""
                val songBpm = intent.getIntExtra("song_bpm", 120)
                val songLatency = intent.getIntExtra("song_latency", 0)
                
                val song = Song(songName, songBpm, songLatency)
                loadSongIntoMetronome(song)
            }
        }
    }
    
    private fun loadCustomSound(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "custom_metronome_sound.wav")
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            if (metronomeEngine.loadCustomSound(file.absolutePath)) {
                updateCustomSoundStatus()
            } else {
                binding.customSoundStatus.text = "Failed to load custom sound"
            }
        } catch (e: Exception) {
            binding.customSoundStatus.text = "Error loading custom sound"
        }
    }
    
    private fun updateCustomSoundStatus() {
        val hasCustomSound = metronomeEngine.getInstrument() == MetronomeEngine.Instrument.CUSTOM
        binding.customSoundStatus.text = if (hasCustomSound) {
            "Custom sound loaded"
        } else {
            "No custom sound loaded"
        }
    }
    
    // Removed startBeatCounter() - now using direct callback system for precise timing
    
    // Removed changeScreenBackgroundColor() - now handled by BeatVisualizer for full-screen strobe effect
    
    
    private fun loadSongIntoMetronome(song: Song) {
        android.util.Log.d("MainActivity", "Loading song into metronome: ${song.name}, BPM: ${song.bpm}, Latency: ${song.latency}")
        
        try {
            binding.tempoSlider.value = song.bpm.toFloat()
            binding.tempoValue.text = song.bpm.toString()
            metronomeEngine.setTempo(song.bpm)
            
            // Load latency if specified
            if (song.latency > 0) {
                setLatencyPreset(song.latency.toLong())
                updateLatencyButtonStates(false, false) // Custom latency
                android.widget.Toast.makeText(this, "✅ Loaded: ${song.name} (${song.bpm} BPM, ${song.latency}ms latency)", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(this, "✅ Loaded: ${song.name} (${song.bpm} BPM)", android.widget.Toast.LENGTH_SHORT).show()
            }
            
            android.util.Log.d("MainActivity", "Song loaded successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading song", e)
            android.widget.Toast.makeText(this, "Error loading song: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isPlaying) {
            stopMetronome()
        }
    }
}
