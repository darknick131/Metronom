@echo off
echo Setting up Android environment...
set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
set PATH=%PATH%;%ANDROID_HOME%\tools\bin;%ANDROID_HOME%\platform-tools

echo.
echo Metronom Android App Build Script
echo ================================
echo.
echo This script will help you build the metronome app.
echo.
echo Prerequisites:
echo - Android Studio installed
echo - Android SDK 35 installed
echo - Java 8 or later
echo.
echo To build the app:
echo 1. Open Android Studio
echo 2. Select "Open an existing project"
echo 3. Navigate to this folder: %CD%
echo 4. Click "OK" to open the project
echo 5. Wait for Gradle sync to complete
echo 6. Click "Run" button or press Shift+F10
echo.
echo Alternative command line build (if gradle wrapper is available):
echo gradlew assembleDebug
echo.
echo The APK will be generated in: app\build\outputs\apk\debug\
echo.
pause
