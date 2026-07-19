@echo off
echo Building PDFX Debug APK locally...
echo.

REM Clean previous build
echo Cleaning previous build...
call gradlew.bat clean

REM Build debug APK
echo Building debug APK...
call gradlew.bat assembleDebug --stacktrace

REM Check if APK was built
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ===================================
    echo SUCCESS! APK built successfully!
    echo ===================================
    echo.
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo You can now transfer this APK to your Redmi 13C and install it.
    echo.
    start app\build\outputs\apk\debug
) else (
    echo.
    echo ===================================
    echo BUILD FAILED!
    echo ===================================
    echo Check the error messages above.
)

pause
