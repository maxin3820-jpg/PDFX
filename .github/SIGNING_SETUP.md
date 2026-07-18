# GitHub Actions Signing Setup

This guide explains how to set up APK signing for GitHub Actions builds.

---

## Overview

PDFX uses GitHub Secrets to securely store signing credentials. The workflow supports both:
- **Debug builds** — built automatically without signing
- **Release builds** — signed with your keystore (if configured)

---

## Step 1: Create a Release Keystore

If you don't have a keystore yet, create one:

```bash
keytool -genkey -v \
  -keystore release-keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias pdfx-release
```

**You'll be prompted for:**
- Keystore password (remember this!)
- Key password (can be same as keystore password)
- Your name, organization, etc.

**Important:** Keep this keystore file safe! You'll need it to sign future updates.

---

## Step 2: Encode Keystore to Base64

Convert your keystore to Base64 for GitHub Secrets:

### On Linux/Mac:
```bash
base64 -i release-keystore.jks -o keystore.txt
```

### On Windows (PowerShell):
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File keystore.txt
```

### On Windows (Git Bash):
```bash
base64 -w 0 release-keystore.jks > keystore.txt
```

---

## Step 3: Add GitHub Secrets

Go to your GitHub repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add these 4 secrets:

| Secret Name | Value | Example |
|-------------|-------|---------|
| `KEYSTORE_BASE64` | Contents of `keystore.txt` | (long base64 string) |
| `KEYSTORE_PASSWORD` | Password you entered when creating keystore | `MySecurePass123` |
| `KEY_ALIAS` | Alias you used (`pdfx-release` in example) | `pdfx-release` |
| `KEY_PASSWORD` | Key password (usually same as keystore password) | `MySecurePass123` |

---

## Step 4: Update build.gradle.kts (Optional)

If you want to sign locally too, add this to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing config ...
    
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**Important:** Add `release-keystore.jks` to `.gitignore` so it's never committed!

---

## Step 5: Trigger a Build

### Automatic (on push):
Push to `main` or `master` branch:
```bash
git add .
git commit -m "Setup GitHub Actions"
git push origin main
```

### Manual (workflow dispatch):
1. Go to **Actions** tab
2. Click **Build APK** workflow
3. Click **Run workflow**
4. Choose build type: `debug` or `release`
5. Click **Run workflow**

---

## Workflow Behavior

### Debug Builds
- Runs on every push to `main`/`master`/`develop`
- No signing required
- Artifact: `pdfx-debug-{run_number}.apk`
- Retention: 30 days

### Release Builds
- Only runs via manual trigger or when secrets are configured
- **With secrets:** Signed release APK
- **Without secrets:** Unsigned release APK (you can sign manually later)
- Artifact: `pdfx-release-{run_number}.apk`
- Retention: 90 days

---

## Downloading Built APKs

After a successful build:

1. Go to **Actions** tab in your repository
2. Click on the workflow run
3. Scroll to **Artifacts** section
4. Download `pdfx-debug-{number}` or `pdfx-release-{number}`
5. Extract the ZIP file
6. Install the APK on your Android device

---

## Security Best Practices

✅ **DO:**
- Store keystore in a password manager
- Use strong, unique passwords
- Add `*.jks`, `*.keystore`, `keystore.txt` to `.gitignore`
- Keep GitHub Secrets private (they're encrypted at rest)

❌ **DON'T:**
- Commit keystore files to Git
- Share keystore passwords in plain text
- Use weak passwords (e.g., "password123")
- Store secrets in code or config files

---

## Troubleshooting

### Build fails with "Keystore was tampered with, or password was incorrect"
- Check `KEYSTORE_PASSWORD` and `KEY_PASSWORD` secrets
- Verify Base64 encoding is correct (no extra whitespace/newlines)

### Build succeeds but APK is unsigned
- Secrets are missing → workflow builds unsigned release APK
- Add secrets as described in Step 3

### "Couldn't find keystore file"
- Ensure `KEYSTORE_BASE64` secret is set
- Check Base64 encoding didn't truncate the file

### Build fails with "Gradle sync failed"
- Check that JDK 17 is being used
- Verify all dependencies are accessible
- Check workflow logs for specific error

---

## Local Signing (for testing)

To sign locally without GitHub Actions:

```bash
# Set environment variables
export KEYSTORE_FILE="./app/release-keystore.jks"
export KEYSTORE_PASSWORD="YourPassword"
export KEY_ALIAS="pdfx-release"
export KEY_PASSWORD="YourPassword"

# Build signed release APK
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

---

## Alternative: Manual Signing

If you prefer not to set up GitHub Secrets, you can:

1. Build unsigned release APK via GitHub Actions
2. Download the APK
3. Sign it manually using `apksigner`:

```bash
# Sign the APK
apksigner sign --ks release-keystore.jks \
  --ks-key-alias pdfx-release \
  --out pdfx-signed.apk \
  pdfx-unsigned.apk

# Verify signature
apksigner verify pdfx-signed.apk
```

---

## Summary

- ✅ GitHub Actions builds APKs automatically
- ✅ Debug builds work out of the box (no setup needed)
- ✅ Release builds can be signed via GitHub Secrets
- ✅ Artifacts are stored for 30-90 days
- ✅ Secure: keystore never committed to Git

For questions, check the workflow logs in the Actions tab.
