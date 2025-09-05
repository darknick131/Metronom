@echo off
echo Fixing Gradle Wrapper...
echo.

REM Create gradle wrapper directory if it doesn't exist
if not exist "gradle\wrapper" mkdir "gradle\wrapper"

echo Downloading Gradle Wrapper JAR...
powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo.
    echo ✅ Gradle wrapper downloaded successfully!
    echo.
    echo Now you can run: .\gradlew.bat assembleDebug
    echo.
) else (
    echo.
    echo ❌ Failed to download Gradle wrapper.
    echo Please use Android Studio instead - it will handle this automatically.
    echo.
)

pause
