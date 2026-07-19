# Build PDFX APK Locally on Your Windows PC

GitHub Actions is having issues. Build the APK on your own computer instead!

## Requirements

1. **Java JDK 17** - Download from: https://adoptium.net/temurin/releases/?version=17
2. **Android SDK** (optional - Gradle will download if needed)

## Simple Build Steps

### Option 1: Using the Build Script (Easiest)

1. Open Command Prompt or PowerShell in the project folder
2. Run:
   ```
   build-local.bat
   ```
3. Wait 5-10 minutes for first build (downloads dependencies)
4. APK will be in: `app\build\outputs\apk\debug\app-debug.apk`

### Option 2: Manual Build

```cmd
gradlew.bat clean
gradlew.bat assembleDebug
```

The APK will be created at:
```
app\build\outputs\apk\debug\app-debug.apk
```

## After Building

1. Copy `app-debug.apk` to your Redmi 13C
2. Enable "Install from Unknown Sources" in Settings
3. Open the APK file to install
4. Launch PDFX app

## Troubleshooting

### "Java not found"
Install Java JDK 17 from: https://adoptium.net/temurin/releases/?version=17

### "SDK location not found"
Create a file named `local.properties` in the project root with:
```
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```
(Replace with your actual Android SDK path)

### Build fails with errors
Run:
```cmd
gradlew.bat assembleDebug --stacktrace --info
```
And share the error output.

## Build Time

- **First build:** 10-15 minutes (downloads ~500MB dependencies)
- **Subsequent builds:** 2-5 minutes

## Result

You'll get: `app-debug.apk` (~8-10 MB)

Transfer to your phone and install!
