package com.metronom.app.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.metronom.app.R

class BeatVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val metronomeBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pendulumArmPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pendulumWeightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tempoScalePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pivotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Metronome dimensions
    private var metronomeBodyRect = RectF()
    private var pendulumPivotX = 0f
    private var pendulumPivotY = 0f
    private var pendulumLength = 0f
    private var pendulumAngle = 0f
    private var pendulumWeightRadius = 0f
    private var tempoScaleRect = RectF()
    
    private var currentBeat = 0
    private var isAnimating = false
    private var pendulumAnimator: ObjectAnimator? = null
    private var backgroundAnimator: ValueAnimator? = null
    
    private val strobeColors = intArrayOf(
        Color.BLACK,
        Color.argb(200, 255, 255, 255) // Brighter white with higher opacity
    )
    private var currentStrobeColor = Color.BLACK
    private var targetStrobeColor = Color.argb(200, 255, 255, 255) // Brighter white
    private var strobeAnimationProgress = 0f
    private var isStrobing = false

    init {
        setupPaints()
    }

    private fun setupPaints() {
        // Simple pendulum arm paint (purple/blue for visibility)
        pendulumArmPaint.color = Color.parseColor("#FF8A2BE2") // Blue violet
        pendulumArmPaint.style = Paint.Style.STROKE
        pendulumArmPaint.strokeWidth = 8f
        pendulumArmPaint.strokeCap = Paint.Cap.ROUND
        pendulumArmPaint.isAntiAlias = true
        
        // Simple pendulum weight paint (purple/blue circle)
        pendulumWeightPaint.color = Color.parseColor("#FF4169E1") // Royal blue
        pendulumWeightPaint.style = Paint.Style.FILL
        pendulumWeightPaint.isAntiAlias = true
        
        // Background paint (for strobe effect)
        backgroundPaint.color = currentStrobeColor
        backgroundPaint.style = Paint.Style.FILL
        
        // Simple shadow paint
        shadowPaint.color = Color.parseColor("#40000000")
        shadowPaint.style = Paint.Style.FILL
        shadowPaint.isAntiAlias = true
        
        // Simple pivot paint (purple/blue)
        pivotPaint.color = Color.parseColor("#FF8A2BE2") // Blue violet
        pivotPaint.style = Paint.Style.FILL
        pivotPaint.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Simple pendulum setup - flipped upside down
        pendulumPivotX = centerX
        pendulumPivotY = centerY + height * 0.1f // Slightly below center (flipped)
        pendulumLength = height * 0.35f // Longer pendulum for better visibility
        pendulumWeightRadius = 25f // Slightly larger weight
        
        // Keep tempo scale for reference but make it minimal
        val scaleWidth = width * 0.6f
        val scaleHeight = 40f
        tempoScaleRect = RectF(
            centerX - scaleWidth / 2f,
            centerY + height * 0.2f,
            centerX + scaleWidth / 2f,
            centerY + height * 0.2f + scaleHeight
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw full screen background - ensure it covers everything
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        
        // Draw simple pendulum only
        drawSimplePendulum(canvas)
    }
    
    private fun drawSimplePendulum(canvas: Canvas) {
        // Flipped pendulum - weight swings upward from pivot
        val pendulumEndX = pendulumPivotX + pendulumLength * kotlin.math.sin(Math.toRadians(pendulumAngle.toDouble())).toFloat()
        val pendulumEndY = pendulumPivotY - pendulumLength * kotlin.math.cos(Math.toRadians(pendulumAngle.toDouble())).toFloat()
        
        // Draw pendulum arm glow effect for visibility
        val armGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        armGlowPaint.color = Color.parseColor("#60FFFFFF")
        armGlowPaint.style = Paint.Style.STROKE
        armGlowPaint.strokeWidth = 12f
        armGlowPaint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(pendulumPivotX, pendulumPivotY, pendulumEndX, pendulumEndY, armGlowPaint)
        
        // Draw pendulum arm shadow (subtle)
        val armShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        armShadowPaint.color = Color.parseColor("#40000000")
        armShadowPaint.style = Paint.Style.STROKE
        armShadowPaint.strokeWidth = 8f
        armShadowPaint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(pendulumPivotX + 2f, pendulumPivotY + 2f, pendulumEndX + 2f, pendulumEndY + 2f, armShadowPaint)
        
        // Draw pendulum arm (vibrant purple/blue line)
        canvas.drawLine(pendulumPivotX, pendulumPivotY, pendulumEndX, pendulumEndY, pendulumArmPaint)
        
        // Draw pendulum weight glow effect
        val weightGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        weightGlowPaint.color = Color.parseColor("#60FFFFFF")
        weightGlowPaint.style = Paint.Style.FILL
        canvas.drawCircle(pendulumEndX, pendulumEndY, pendulumWeightRadius + 4f, weightGlowPaint)
        
        // Draw pendulum weight shadow (subtle)
        val weightShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        weightShadowPaint.color = Color.parseColor("#50000000")
        weightShadowPaint.style = Paint.Style.FILL
        canvas.drawCircle(pendulumEndX + 2f, pendulumEndY + 2f, pendulumWeightRadius, weightShadowPaint)
        
        // Draw pendulum weight (vibrant blue circle)
        canvas.drawCircle(pendulumEndX, pendulumEndY, pendulumWeightRadius, pendulumWeightPaint)
        
        // Draw pivot point glow
        val pivotGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pivotGlowPaint.color = Color.parseColor("#60FFFFFF")
        pivotGlowPaint.style = Paint.Style.FILL
        canvas.drawCircle(pendulumPivotX, pendulumPivotY, 12f, pivotGlowPaint)
        
        // Draw pivot point (vibrant purple circle)
        canvas.drawCircle(pendulumPivotX, pendulumPivotY, 8f, pivotPaint)
    }

    // Removed complex tempo markings - keeping it simple with just the pendulum

    // Removed complex pendulum and metronome base methods - using simple pendulum only

    fun startBeatAnimation(bpm: Int) {
        if (isAnimating) return
        
        isAnimating = true
        val beatInterval = (60000.0 / bpm).toLong() // Convert BPM to milliseconds per beat
        
        // Create perfectly synchronized pendulum swing animation
        pendulumAnimator = ObjectAnimator.ofFloat(this, "pendulumAngle", -60f, 60f)
        pendulumAnimator?.apply {
            duration = beatInterval / 2 // Half beat for swing, then reverse
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            // Use linear interpolator for precise timing with audio
            interpolator = android.view.animation.LinearInterpolator()
            start()
        }
        
        // Start full screen strobe effect (synchronized with pendulum)
        startFullScreenStrobe()
    }

    fun updateTempo(bpm: Int) {
        if (isAnimating) {
            // Restart animation with new tempo
            stopBeatAnimation()
            startBeatAnimation(bpm)
        }
    }

    fun stopBeatAnimation() {
        isAnimating = false
        isStrobing = false
        pendulumAnimator?.cancel()
        backgroundAnimator?.cancel()
        pendulumAnimator = null
        backgroundAnimator = null
        
        // Reset pendulum to center
        pendulumAngle = 0f
        
        // Reset to black background
        currentStrobeColor = Color.BLACK
        targetStrobeColor = Color.argb(128, 255, 255, 255) // White with 50% opacity
        strobeAnimationProgress = 0f
        backgroundPaint.color = Color.BLACK
        
        // Reset entire screen to black
        resetEntireScreenToBlack()
        invalidate()
    }

    private fun startFullScreenStrobe() {
        isStrobing = true
        // Start with black background
        currentStrobeColor = Color.BLACK
        targetStrobeColor = Color.WHITE
        strobeAnimationProgress = 0f
        backgroundPaint.color = Color.BLACK
        
        // Set entire screen to black initially
        changeEntireScreenBackground(Color.BLACK)
    }

    private fun updateStrobeColor() {
        // Simple black/white strobe - no interpolation needed
        backgroundPaint.color = currentStrobeColor
    }

    fun onBeat() {
        if (!isAnimating || !isStrobing) return
        
        currentBeat = (currentBeat + 1) % 4
        
        currentStrobeColor = if (currentStrobeColor == Color.BLACK) {
            Color.argb(255, 255, 255, 255) // Pure white flash
        } else {
            Color.BLACK
        }
        targetStrobeColor = if (currentStrobeColor == Color.BLACK) {
            Color.argb(255, 255, 255, 255) // Pure white flash
        } else {
            Color.BLACK
        }
        
        backgroundPaint.color = currentStrobeColor
        
        // Change entire screen background IMMEDIATELY - no delays
        changeEntireScreenBackground(currentStrobeColor)
        
        if (currentStrobeColor == Color.BLACK) {
            // During black flash, make pendulum extra bright
            pendulumArmPaint.color = Color.parseColor("#FFFF00FF") // Bright magenta
            pendulumWeightPaint.color = Color.parseColor("#FF00FFFF") // Bright cyan
            pivotPaint.color = Color.parseColor("#FFFF00FF") // Bright magenta
        } else {
            // During white flash, use contrasting colors
            pendulumArmPaint.color = Color.parseColor("#FF0000FF") // Bright blue
            pendulumWeightPaint.color = Color.parseColor("#FF8A2BE2") // Blue violet
            pivotPaint.color = Color.parseColor("#FF0000FF") // Bright blue
        }
        
        // Force immediate redraw
        post { invalidate() }
        
        // Reset pendulum colors after a short delay for better visibility
        postDelayed({
            pendulumArmPaint.color = Color.parseColor("#FF8A2BE2") // Blue violet
            pendulumWeightPaint.color = Color.parseColor("#FF4169E1") // Royal blue
            pivotPaint.color = Color.parseColor("#FF8A2BE2") // Blue violet
            invalidate()
        }, 100) // Reset after 100ms
    }
    
    private fun changeEntireScreenBackground(color: Int) {
        val activity = context as? android.app.Activity ?: return
        
        // Change the entire screen background - covers everything
        activity.window.decorView.setBackgroundColor(color)
        
        // Change the root layout background
        try {
            val rootView = activity.findViewById<android.view.View>(android.R.id.content)
            rootView?.setBackgroundColor(color)
        } catch (e: Exception) {
            // Ignore if we can't find the root view
        }
        
        // Change the main container background to ensure full coverage
        try {
            val mainContainer = activity.findViewById<android.view.View>(R.id.mainContainer)
            mainContainer?.setBackgroundColor(color)
        } catch (e: Exception) {
            // Ignore if we can't find the main container
        }
        
        // Change the scroll view background
        try {
            val scrollView = activity.findViewById<android.view.View>(R.id.rootScrollView)
            scrollView?.setBackgroundColor(color)
        } catch (e: Exception) {
            // Ignore if we can't find the scroll view
        }
    }
    
    private fun resetEntireScreenToBlack() {
        val activity = context as? android.app.Activity ?: return
        
        // Reset entire screen to black - covers everything
        activity.window.decorView.setBackgroundColor(Color.BLACK)
        
        // Reset the root layout background
        try {
            val rootView = activity.findViewById<android.view.View>(android.R.id.content)
            rootView?.setBackgroundColor(Color.BLACK)
        } catch (e: Exception) {
            // Ignore if we can't find the root view
        }
        
        // Reset the main container background
        try {
            val mainContainer = activity.findViewById<android.view.View>(R.id.mainContainer)
            mainContainer?.setBackgroundColor(Color.BLACK)
        } catch (e: Exception) {
            // Ignore if we can't find the main container
        }
        
        // Reset the scroll view background
        try {
            val scrollView = activity.findViewById<android.view.View>(R.id.rootScrollView)
            scrollView?.setBackgroundColor(Color.BLACK)
        } catch (e: Exception) {
            // Ignore if we can't find the scroll view
        }
    }

    // Property setter for animation
    fun setPendulumAngle(angle: Float) {
        pendulumAngle = angle
        invalidate()
    }
}