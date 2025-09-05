# 🎵 Metronome App - Comprehensive Feature Guide

## 📱 **App Overview**
Your metronome app is a professional music beat visualizer with advanced features including:
- **Flipped upside-down pendulum** (purple/blue colors)
- **Perfect BPM accuracy** (nanosecond precision timing)
- **Dramatic striking effect** (black/white full-screen flashes)
- **Dynamic color changes** during flashes
- **Professional audio engine** with multiple instruments
- **Song library** with save/load functionality
- **Precise synchronization** between audio and visuals

---

## 🎯 **Main Features & Locations**

### **1. Beat Visualizer (Pendulum Animation)**
**Location**: `app/src/main/java/com/metronom/app/ui/BeatVisualizerView.kt`

**What it does**: 
- Displays a flipped upside-down pendulum that swings in sync with the BPM
- Changes colors during screen flashes for visibility
- Handles full-screen strobe effects

**Key Customization Points**:
```kotlin
// Pendulum Colors (Lines 50-60)
private val pendulumArmPaint = Paint().apply {
    color = Color.parseColor("#FF8A2BE2") // Purple arm
    strokeWidth = 8f
}

private val pendulumWeightPaint = Paint().apply {
    color = Color.parseColor("#FF4169E1") // Blue weight
}

// Strobe Colors (Lines 65-70)
private val strobeColors = arrayOf(
    Color.BLACK,                    // Black flash
    Color.argb(255, 255, 255, 255) // White flash
)

// Pendulum Swing Angle (Line 200)
val animator = ObjectAnimator.ofFloat(this, "pendulumAngle", -60f, 60f)
```

**How to Customize**:
- **Change pendulum colors**: Modify `pendulumArmPaint.color` and `pendulumWeightPaint.color`
- **Adjust swing angle**: Change the values in `ObjectAnimator.ofFloat(this, "pendulumAngle", -60f, 60f)`
- **Modify strobe colors**: Update the `strobeColors` array
- **Change pendulum size**: Adjust `pendulumLength` calculation in `onSizeChanged()`

### **2. Audio Engine**
**Location**: `app/src/main/java/com/metronom/app/MetronomeEngine.kt`

**What it does**:
- Generates precise audio clicks with nanosecond timing
- Supports multiple instruments (Guitar, Bass, Drums, Custom)
- Handles latency compensation
- Manages custom sound files

**Key Customization Points**:
```kotlin
// BPM Range (Line 57)
fun setTempo(newTempo: Int) {
    _tempo = newTempo.coerceIn(40, 200) // Change range here
}

// Audio Quality (Lines 45-50)
private val sampleRate = 44100 // Higher = better quality, more CPU
private val bufferSize = AudioTrack.getMinBufferSize(...)

// Click Duration (Line 53)
private val clickDuration = 0.1f // seconds - how long each click lasts

// Instrument Frequencies (Lines 189-194)
val frequency = when (instrument) {
    Instrument.GUITAR -> 1000f  // Hz
    Instrument.BASS -> 800f     // Hz  
    Instrument.DRUMS -> 1200f   // Hz
    Instrument.CUSTOM -> 1000f
}
```

**How to Customize**:
- **Change BPM range**: Modify `coerceIn(40, 200)` in `setTempo()`
- **Adjust audio quality**: Change `sampleRate` (44100, 48000, 96000)
- **Modify click duration**: Update `clickDuration` value
- **Change instrument sounds**: Modify frequency values or sound generation functions
- **Add new instruments**: Add to `Instrument` enum and `when` statements

### **3. Main UI Controls**
**Location**: `app/src/main/java/com/metronom/app/MainActivity.kt`

**What it does**:
- Manages all user interactions
- Coordinates between audio engine and visualizer
- Handles song loading and custom sound uploads

**Key Customization Points**:
```kotlin
// Latency Presets (Lines 31-32)
private val speakersLatency = 50L  // milliseconds
private val headphonesLatency = 20L // milliseconds

// Tempo Slider Range (Lines 48-55)
binding.tempoSlider.addOnChangeListener { _, value, _ ->
    val tempo = value.toInt()
    // Range is set in XML: android:valueFrom="40" android:valueTo="200"
}
```

**How to Customize**:
- **Change default latency**: Modify `speakersLatency` and `headphonesLatency`
- **Adjust tempo range**: Update XML in `activity_main.xml` lines 152-154
- **Add new sound instruments**: Add buttons and handlers in `setupUI()`

### **4. Song Library System**
**Location**: `app/src/main/java/com/metronom/app/SimpleSongManager.kt` & `SongListActivity.kt`

**What it does**:
- Saves/loads songs with BPM and latency settings
- Provides CRUD operations for song management
- Persists data using SharedPreferences

**Key Customization Points**:
```kotlin
// Song Data Structure (Lines 56-60)
data class Song(
    val name: String,
    val bpm: Int,
    val latency: Int = 0
)

// BPM Validation (Lines 138-143)
val bpm = try {
    bpmText.toInt().coerceIn(40, 200) // Change range here
} catch (e: NumberFormatException) {
    // Error handling
}
```

**How to Customize**:
- **Add new song properties**: Extend the `Song` data class
- **Change BPM validation**: Update `coerceIn(40, 200)` values
- **Modify storage method**: Replace SharedPreferences with Room database
- **Add song categories**: Add a `category` field to `Song`

### **5. UI Layout & Styling**
**Location**: `app/src/main/res/layout/activity_main.xml`

**What it does**:
- Defines the main app layout
- Sets up all UI components and their positioning
- Handles responsive design

**Key Customization Points**:
```xml
<!-- Tempo Slider Range (Lines 152-154) -->
<com.google.android.material.slider.Slider
    android:valueFrom="40"
    android:valueTo="200"
    android:value="120" />

<!-- Beat Visualizer Size (Lines 95-102) -->
<com.metronom.app.ui.BeatVisualizerView
    android:layout_height="200dp" />

<!-- Color Scheme (Lines 8, 16, 26) -->
android:background="@color/black"
app:backgroundTint="@color/blue_accent"
app:backgroundTint="@color/purple_accent"
```

**How to Customize**:
- **Change tempo range**: Update `valueFrom` and `valueTo` in tempo slider
- **Adjust visualizer size**: Modify `layout_height` of `BeatVisualizerView`
- **Update color scheme**: Change color references throughout the layout
- **Add new UI elements**: Add new views and position them with constraints

---

## 🎨 **Visual Customization Guide**

### **Colors & Themes**
**Location**: `app/src/main/res/values/colors.xml`

```xml
<!-- Main Colors -->
<color name="black">#FF000000</color>
<color name="white">#FFFFFFFF</color>

<!-- Accent Colors -->
<color name="blue_accent">#FF2196F3</color>
<color name="purple_accent">#FF9C27B0</color>
<color name="red_accent">#FFF44336</color>

<!-- Transparency Levels -->
<color name="white_alpha_10">#1AFFFFFF</color>
<color name="white_alpha_30">#4DFFFFFF</color>
<color name="white_alpha_50">#80FFFFFF</color>
```

**How to Change**:
- **App background**: Change `@color/black` in `activity_main.xml`
- **Button colors**: Update `@color/blue_accent` and `@color/purple_accent`
- **Text colors**: Modify `@color/white` references
- **Add new colors**: Add entries to `colors.xml` and reference them

### **Pendulum Visual Effects**
**Location**: `BeatVisualizerView.kt` lines 200-250

```kotlin
// Flash Colors During Animation
private fun onBeat() {
    currentStrobeColor = if (currentStrobeColor == strobeColors[0]) strobeColors[1] else strobeColors[0]
    
    // Dynamic pendulum colors during flash
    if (currentStrobeColor == Color.BLACK) {
        pendulumArmPaint.color = Color.parseColor("#FFFF00FF") // Magenta
        pendulumWeightPaint.color = Color.parseColor("#FF00FFFF") // Cyan
    } else {
        pendulumArmPaint.color = Color.parseColor("#FF0000FF") // Blue
        pendulumWeightPaint.color = Color.parseColor("#FF8A2BE2") // Blue Violet
    }
}
```

**How to Customize**:
- **Change flash colors**: Modify `strobeColors` array
- **Adjust pendulum colors**: Update color values in `onBeat()`
- **Add glow effects**: Modify `armGlowPaint`, `weightGlowPaint` properties
- **Change animation speed**: Adjust `duration` in `startBeatAnimation()`

---

## ⚙️ **Technical Customization Guide**

### **Audio Quality Settings**
**Location**: `MetronomeEngine.kt` lines 45-50

```kotlin
private val sampleRate = 44100 // CD Quality
// Options: 22050 (Low), 44100 (CD), 48000 (Studio), 96000 (High-Res)
```

### **Timing Precision**
**Location**: `MetronomeEngine.kt` lines 146-175

```kotlin
// Nanosecond precision timing
val intervalNs = (60000000000.0 / _tempo).toLong()
var nextBeatTime = System.nanoTime()

// Accumulative timing to prevent drift
nextBeatTime += intervalNs
```

### **Memory Management**
**Location**: `MainActivity.kt` lines 275-280

```kotlin
override fun onDestroy() {
    super.onDestroy()
    if (isPlaying) {
        stopMetronome() // Clean up resources
    }
}
```

---

## 🚀 **Advanced Customization Examples**

### **1. Add New Instrument**
```kotlin
// In MetronomeEngine.kt
enum class Instrument {
    GUITAR, BASS, DRUMS, CUSTOM, PIANO // Add new instrument
}

// Add frequency
val frequency = when (instrument) {
    Instrument.GUITAR -> 1000f
    Instrument.BASS -> 800f
    Instrument.DRUMS -> 1200f
    Instrument.PIANO -> 1500f // Add new frequency
    Instrument.CUSTOM -> 1000f
}

// Add sound generation
private fun generatePianoClick(time: Float, frequency: Float, isAccent: Boolean): Float {
    val envelope = exp(-time * 12f).toFloat()
    val wave = sin(2 * PI * frequency * time).toFloat()
    val baseAmplitude = if (isAccent) 0.5f else 0.4f
    return envelope * wave * baseAmplitude
}
```

### **2. Change Pendulum Style**
```kotlin
// In BeatVisualizerView.kt
private fun drawSimplePendulum(canvas: Canvas) {
    // Change from simple pendulum to metronome body
    // Add visual metronome case, tempo markings, etc.
}
```

### **3. Add Visual Effects**
```kotlin
// Add particle effects, trails, or other visual enhancements
private fun drawParticleEffects(canvas: Canvas) {
    // Custom drawing code for additional effects
}
```

### **4. Modify Strobe Patterns**
```kotlin
// In BeatVisualizerView.kt
private val strobeColors = arrayOf(
    Color.BLACK,
    Color.WHITE,
    Color.parseColor("#FF00FF00"), // Add green
    Color.parseColor("#FFFF0000")  // Add red
)
```

---

## 📁 **File Structure Reference**

```
app/src/main/
├── java/com/metronom/app/
│   ├── MainActivity.kt              # Main UI logic
│   ├── MetronomeEngine.kt           # Audio engine
│   ├── SimpleSongManager.kt         # Song data management
│   ├── SongListActivity.kt          # Song library UI
│   └── ui/
│       └── BeatVisualizerView.kt    # Pendulum & strobe effects
├── res/
│   ├── layout/
│   │   ├── activity_main.xml        # Main layout
│   │   ├── activity_song_list.xml   # Song library layout
│   │   ├── item_song.xml           # Song item layout
│   │   └── dialog_song_edit.xml    # Song edit dialog
│   ├── values/
│   │   ├── colors.xml              # Color definitions
│   │   └── strings.xml             # Text strings
│   └── drawable/                   # Icons and backgrounds
└── AndroidManifest.xml             # App configuration
```

---

## 🔧 **Build & Deployment**

### **Generate APK**
```bash
# Debug APK (for testing)
.\gradlew.bat assembleDebug

# Release APK (for distribution)
.\gradlew.bat assembleRelease
```

### **APK Location**
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎵 **Feature Summary**

| Feature | Location | Customization Level |
|---------|----------|-------------------|
| **Pendulum Animation** | `BeatVisualizerView.kt` | High - Colors, size, swing angle |
| **Strobe Effects** | `BeatVisualizerView.kt` | High - Colors, patterns, timing |
| **Audio Engine** | `MetronomeEngine.kt` | High - Quality, instruments, timing |
| **UI Layout** | `activity_main.xml` | Medium - Layout, colors, sizing |
| **Song Library** | `SongListActivity.kt` | Medium - Features, validation |
| **Color Scheme** | `colors.xml` | Low - Simple color changes |
| **Text/Strings** | `strings.xml` | Low - Text content only |

---

## 💡 **Pro Tips**

1. **For Visual Changes**: Start with `BeatVisualizerView.kt` and `colors.xml`
2. **For Audio Changes**: Modify `MetronomeEngine.kt` audio parameters
3. **For UI Changes**: Update `activity_main.xml` layout
4. **For New Features**: Add to `MainActivity.kt` and create supporting files
5. **Always test**: Build and test changes incrementally
6. **Backup**: Keep copies of working versions before major changes

This guide covers all the major features and customization points in your metronome app. Each section includes specific file locations, code examples, and step-by-step instructions for modifications. 🎵⚡️
