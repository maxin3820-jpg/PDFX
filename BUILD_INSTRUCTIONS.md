# Build Instructions

## Prerequisites

- **Android Studio Hedgehog** (2023.1.1) or newer — [download](https://developer.android.com/studio)
- **JDK 17** — bundled with Android Studio or install separately
- **Android SDK 34** — install via Android Studio's SDK Manager

---

## Step 1 — Open in Android Studio

1. Launch Android Studio
2. `File → Open` → select the `PDFX` folder
3. Wait for Gradle sync to complete (first sync downloads dependencies, ~2–5 min)

Android Studio will automatically:
- Download the `gradle-wrapper.jar` via Gradle Wrapper
- Resolve all dependencies from Maven Central and Google
- Generate Hilt and Room annotation processor outputs

---

## Step 2 — Configure `local.properties`

Android Studio creates this automatically. If building from CLI:

```
# Windows
sdk.dir=C\:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk

# macOS / Linux
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

---

## Step 3 — Run Debug Build

### From Android Studio:
- Select a device or emulator (API 26+)
- Press ▶ Run

### From terminal:
```bash
# macOS / Linux
./gradlew installDebug

# Windows
gradlew.bat installDebug
```

---

## Step 4 — Release Build

1. Create a signing keystore:
   ```bash
   keytool -genkey -v -keystore pdfx-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pdfx
   ```

2. Add signing config to `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("pdfx-release.jks")
               storePassword = "YOUR_STORE_PASSWORD"
               keyAlias = "pdfx"
               keyPassword = "YOUR_KEY_PASSWORD"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
               // ... existing config
           }
       }
   }
   ```
   > Store passwords in environment variables or a `keystore.properties` file that is git-ignored.

3. Build:
   ```bash
   ./gradlew assembleRelease
   # Output: app/build/outputs/apk/release/app-release.apk
   ```

---

## Gradle wrapper jar

If `gradle/wrapper/gradle-wrapper.jar` is missing (it's excluded from git), regenerate it:

```bash
gradle wrapper --gradle-version 8.7
```

Or let Android Studio handle it automatically on first sync.

---

## Dependencies summary

All dependencies are resolved from Maven Central and Google Maven. No local AAR files or private repositories needed.

| Dependency | Version |
|---|---|
| Kotlin | 1.9.23 |
| AGP | 8.3.2 |
| Compose BOM | 2024.05.00 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| Navigation Compose | 2.7.7 |
| DataStore | 1.1.1 |
| Coil | 2.6.0 |
| Core Splash Screen | 1.0.1 |
