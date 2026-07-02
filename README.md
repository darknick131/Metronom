# Metronom

**A precision metronome for Android built for live band rehearsal — high-visibility full-screen strobe effect, nanosecond-accurate beat timing, and per-song BPM/latency presets.**

![Platform](https://img.shields.io/badge/platform-Android%207.0%2B-brightgreen)
![Language](https://img.shields.io/badge/language-Kotlin-7F52FF)
![Build System](https://img.shields.io/badge/build-Gradle%208-02303A)
![Min SDK](https://img.shields.io/badge/minSdk-24-informational)
![Target SDK](https://img.shields.io/badge/targetSdk-35-informational)

---

## Screenshot

> _Add a screenshot or screen recording here._

---

## Overview

Metronom is a native Android metronome designed for band practice situations where standard apps fall short. It generates click sounds directly as PCM audio via `AudioTrack` in streaming mode to keep playback latency as low as possible, and tracks beat intervals using `System.nanoTime()` to prevent tempo drift over long sessions.

The visualizer is a full-screen strobe: on every beat the entire screen background flips between black and white, paired with an animated pendulum. The strobe is intentionally aggressive so it remains visible from across a rehearsal room, even in ambient light.

A song library lets you save a name, BPM, and latency offset per song so you can load the exact right settings before starting a take or a section of a setlist, without manually re-entering numbers each time.

---

## Key Features

- **Nanosecond-precision timing** — the beat loop runs on a dedicated IO coroutine and tracks the next beat time in nanoseconds (`System.nanoTime()`), accumulating rather than resetting each beat to prevent drift.
- **Full-screen strobe on beat** — `BeatVisualizerView` changes the Activity window background and root layout to black or white on every beat callback, not just a small indicator. Colors alternate each beat.
- **Animated pendulum** — custom `Canvas`-drawn pendulum swings ±60° in sync with the tempo using `ObjectAnimator` and `LinearInterpolator`.
- **Beat accent** — the first beat of each measure plays at 1.5× amplitude.
- **Three synthesized click timbres:**
  - Click (Guitar) — 1000 Hz sine, fast exponential decay (`exp(-15t)`)
  - Beep (Bass) — 800 Hz sine, slower decay (`exp(-8t)`)
  - Tick (Drums) — 1200 Hz sine + white noise, very fast decay (`exp(-25t)`)
- **Custom audio file** — upload any audio file from device storage; it gets copied to the app cache and played as raw bytes through `AudioTrack`.
- **Latency compensation** — manual slider (0–200 ms) plus quick presets: Speakers (50 ms) and Earbuds (20 ms), applied as a fixed offset in the timing loop.
- **Time signatures** — engine supports 4/4, 3/4, and 2/4 (beat counter and accent reset correctly per measure).
- **Song library** — save song presets (name, BPM, optional latency offset); load, edit, or delete them from a RecyclerView list. Persisted synchronously to `SharedPreferences` as a JSON array.
- **Beat counter** — on-screen number that updates on every beat callback from the audio thread, synchronized with audio output rather than with a UI timer.

---

## System Architecture & Data Flow

The app is two Activities. There are no Fragments, no ViewModels, and no Repository layer — logic lives directly in the Activity and engine classes.

```
User input (slider / button)
        │
        ▼
  MainActivity
  (ViewBinding, UI state)
        │
        ├──► MetronomeEngine.setTempo() / setInstrument() / setLatency()
        │
        └──► MetronomeEngine.start()
                    │
                    ▼
         Coroutine on Dispatchers.IO
         System.nanoTime() timing loop
                    │
                    ▼
         generateClickSound()
         AudioTrack.write(pcmBytes)     ← raw PCM to hardware
                    │
                    ▼
         BeatCallback.onBeat(beatNumber)
                    │
                    ▼ (runOnUiThread)
         ┌──────────────────────────────┐
         │  beatCounter.text = beat     │
         │  BeatVisualizerView.onBeat() │
         │    → window background flip  │
         │    → invalidate()            │
         └──────────────────────────────┘

ObjectAnimator (UI thread, independent)
  pendulumAngle -60° ↔ +60°
  duration = beatInterval / 2
  LinearInterpolator
```

**Song Library flow:**

```
MainActivity ──startActivityForResult──► SongListActivity
                                               │
                                    SimpleSongManager (SharedPreferences)
                                    getSongs() / saveSong() / deleteSong()
                                               │
                                    ◄──setResult(RESULT_SONG_LOADED, intent)──
                                               │
                               ◄─── onActivityResult ───
                               loadSongIntoMetronome(song)
                               tempoSlider.value = song.bpm
                               setLatencyPreset(song.latency)
```

---

## Project Structure

```
Metronom/
├── app/
│   ├── src/main/
│   │   ├── java/com/metronom/app/
│   │   │   ├── MainActivity.kt          # Entry point; UI wiring, button handlers, Activity lifecycle
│   │   │   ├── MetronomeEngine.kt       # Audio engine: PCM generation, nanoTime timing loop, BeatCallback
│   │   │   ├── SimpleSongManager.kt     # SharedPreferences read/write; Song data class
│   │   │   ├── SongListActivity.kt      # Song library screen; RecyclerView adapter; add/edit/delete dialogs
│   │   │   └── ui/
│   │   │       └── BeatVisualizerView.kt  # Custom View: Canvas pendulum drawing + strobe effect
│   │   └── res/
│   │       ├── layout/                  # activity_main, activity_song_list, dialog_song_edit, item_song*
│   │       ├── drawable/                # Vector icons + shape backgrounds (beat counter, gradient, etc.)
│   │       ├── values/                  # colors.xml (dark theme palette), strings.xml, themes.xml
│   │       └── mipmap-*/               # App icons in 5 density buckets
│   └── build.gradle                    # Module-level: dependencies, SDK versions, ViewBinding flag
├── gradle/wrapper/                      # Gradle wrapper JAR + properties
├── build.gradle                         # Top-level: AGP + Kotlin plugin versions
├── settings.gradle                      # Project name, module includes, repository config
├── gradle.properties                    # JVM args, AndroidX and R class flags
├── gradlew / gradlew.bat                # Gradle wrapper entry points
└── local.properties                     # Local SDK path — not committed
```

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Kotlin 1.9.10 | Coroutines enable a clean IO-thread timing loop without thread management boilerplate |
| Platform | Android SDK (min 24 / target 35) | Native Android; `AudioTrack` is not available cross-platform |
| Audio output | `AudioTrack` (MODE_STREAM, PCM 16-bit, 44100 Hz mono) | Direct buffer writes bypass MediaPlayer overhead; lowest achievable playback latency |
| Beat timing | `System.nanoTime()` + `Dispatchers.IO` coroutine | Nanosecond clock avoids drift; IO dispatcher keeps audio off the main thread |
| UI binding | ViewBinding | Compile-time null-safe view references; no reflection at runtime |
| Layout | ConstraintLayout + Material Design 3 (1.11.0) | Sliders, MaterialButton, FAB, dialogs from the Material component library |
| Graphics | `Canvas` API (custom `View`) | Pendulum drawn entirely in software; no external graphics library needed |
| Animation | `ObjectAnimator` / `ValueAnimator` | Property animation for pendulum swing; synchronized to tempo interval |
| Persistence | `SharedPreferences` + `org.json` | Lightweight; songs are serialized as a JSON array string |
| Build | AGP 8.7.1 / Gradle 8 | Standard Android build toolchain |
| Coroutines | kotlinx-coroutines-android 1.7.3 | Async timing loop and UI dispatch |

---

## Getting Started

### Prerequisites

- **Android Studio** Koala (2024.1) or newer
- **JDK 8** or higher (project targets `VERSION_1_8`)
- Android device or emulator running **Android 7.0 (API 24)** or higher
- USB debugging enabled if using a physical device

### Install and Run

```bash
# 1. Clone
git clone https://github.com/darknick131/Metronom.git
cd Metronom

# 2. Build a debug APK
./gradlew assembleDebug

# 3. Install on a connected device
./gradlew installDebug
```

Or open the project in Android Studio and press **Run ▶**.

The app requests `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, and `WAKE_LOCK` permissions at install time. `WAKE_LOCK` prevents the screen and CPU from sleeping during a long practice session.

---

## Usage

### Basic metronome

1. Drag the **BPM slider** to set tempo (40–200 BPM). The large number updates in real time.
2. Choose a click sound: **Click**, **Beep**, or **Tick**.
3. Tap **Start**. The pendulum begins swinging and the screen strobes on every beat.
4. Tap **Stop** to halt playback and reset the visualizer to black.

### Latency compensation

If the strobe feels out of sync with what you hear through your monitoring setup:

- Tap **Speakers** (sets 50 ms) or **Earbuds** (sets 20 ms) for a quick preset.
- Use the latency slider to fine-tune between 0–200 ms.

The offset shifts the timing loop's next-beat target forward by the configured amount.

### Custom click sound

1. Tap **Upload** and select any audio file from your device.
2. The file is copied to the app cache and played through `AudioTrack` as raw bytes in place of the synthesized click.
3. Tap **Clear** to return to the synthesized sounds.

### Song library

1. Tap **Song Library** to open the list.
2. Tap **+** to add a new entry (name, BPM, and optional latency offset).
3. Tap **Load** on any row to apply that song's settings to the main screen and return.
4. Tap **Edit** or **Delete** to modify or remove an entry.

---

## Roadmap

The following are visible in the codebase as commented-out code or implemented in the engine but without UI:

- **Room database** — `androidx.room` dependencies are present but commented out in `app/build.gradle`. `SimpleSongManager` currently uses `SharedPreferences`.
- **Additional time signatures** — `MetronomeEngine.TimeSignature` defines 3/4 and 2/4 (with correct beat counting and accent), but the main screen has no UI control to switch between them. Only 4/4 is reachable by default.

---

## Assumptions to Verify

The following claims appear in the previous README but are **not supported by the current code** — verify before reinstating them:

| Claim | Reality found in code |
|---|---|
| Piano as a fourth instrument | `MetronomeEngine.Instrument` has `GUITAR`, `BASS`, `DRUMS`, `CUSTOM` — no Piano |
| MVVM / Clean architecture | No `ViewModel` or Repository classes exist; logic is in the two Activities directly |
| Physics-based pendulum simulation | `ObjectAnimator` with `LinearInterpolator`, fixed ±60° — no physics equations |
| +/- BPM buttons | `activity_main.xml` has only a slider; no increment/decrement buttons |
| Latency range 0–500 ms | UI slider `valueTo="200"`; engine accepts up to 500 ms but UI caps at 200 ms |

---

## Contributing

Pull requests are welcome. Open an issue first for anything beyond a small bug fix.

---

## License

No license file is present in the repository. Add one before accepting contributions.

---

## Contact

- GitHub: [@darknick131](https://github.com/darknick131)
