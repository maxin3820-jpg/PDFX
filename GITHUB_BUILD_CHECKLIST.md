# GitHub Actions Build Checklist

Complete checklist to ensure **zero-error** APK builds on GitHub Actions.

---

## Pre-Push Checklist

Use this before pushing code to catch issues early.

### ✅ Repository Setup

- [ ] GitHub repository created
- [ ] Repository is public or has GitHub Actions enabled
- [ ] Local repository initialized (`git init`)
- [ ] Remote added (`git remote add origin <url>`)

### ✅ Required Files Present

- [ ] `gradlew` (Linux/Mac wrapper)
- [ ] `gradlew.bat` (Windows wrapper)
- [ ] `gradle/wrapper/gradle-wrapper.properties`
- [ ] `gradle/wrapper/gradle-wrapper.jar`
- [ ] `build.gradle.kts` (root)
- [ ] `settings.gradle.kts`
- [ ] `gradle.properties`
- [ ] `app/build.gradle.kts`
- [ ] `app/src/main/AndroidManifest.xml`

### ✅ GitHub Workflows

- [ ] `.github/workflows/debug-apk.yml` exists
- [ ] `.github/workflows/build-apk.yml` exists
- [ ] `.github/workflows/release-apk.yml` exists
- [ ] `.github/workflows/pr-checks.yml` exists (optional)

### ✅ Security (CRITICAL)

- [ ] `.gitignore` includes `local.properties`
- [ ] `.gitignore` includes `*.jks`
- [ ] `.gitignore` includes `*.keystore`
- [ ] `local.properties` NOT committed to git
- [ ] Keystore files NOT committed to git
- [ ] Passwords NOT in source code

**Verify with:**
```bash
git ls-files | grep -E "(local.properties|\.jks|\.keystore)"
```
Should return nothing!

### ✅ Build Configuration

- [ ] `compileSdk = 34` in `app/build.gradle.kts`
- [ ] `minSdk = 26` (Android 8.0+)
- [ ] `targetSdk = 34`
- [ ] JDK 17 specified in workflows
- [ ] ProGuard rules configured for release
- [ ] No hardcoded API keys or secrets

### ✅ Source Code

- [ ] All `.kt` files compile locally
- [ ] No syntax errors
- [ ] All imports resolve
- [ ] No TODO/FIXME that break builds
- [ ] AndroidManifest.xml is valid XML

### ✅ Local Build Test

Run these commands before pushing:

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned is fine)
./gradlew assembleRelease

# Check for lint issues (optional)
./gradlew lintDebug
```

All should complete without errors.

---

## First-Time Push Checklist

### ✅ Before First Push

- [ ] Verify build script: `bash verify-build.sh` (Linux/Mac) or `verify-build.bat` (Windows)
- [ ] Review `.gitignore` — no sensitive files
- [ ] Commit all changes: `git add . && git commit -m "Initial commit"`
- [ ] Push to GitHub: `git push -u origin main`

### ✅ After First Push

- [ ] Go to repository → **Actions** tab
- [ ] Verify workflows appear
- [ ] Wait for **Build Debug APK** to start (auto-triggered)
- [ ] Monitor build progress (~5-10 minutes)
- [ ] Check for green checkmark ✅

### ✅ If Build Succeeds

- [ ] Download artifact from Actions tab
- [ ] Extract APK
- [ ] Install on Android device/emulator
- [ ] Verify app launches and works
- [ ] 🎉 Success!

### ✅ If Build Fails

1. Click on failed workflow
2. Expand failed step
3. Read error message
4. Fix locally
5. Test: `./gradlew assembleDebug`
6. Commit and push fix
7. Verify build succeeds

---

## Release Build Checklist

For signed production APKs.

### ✅ Keystore Preparation

- [ ] Android keystore file exists (`release-keystore.jks`)
- [ ] You know the keystore password
- [ ] You know the key alias
- [ ] You know the key password
- [ ] Keystore is NOT committed to git

**Generate if needed:**
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pdfx-release
```

### ✅ Encode Keystore to Base64

**Linux/Mac:**
```bash
base64 -i release-keystore.jks | tr -d '\n' > keystore.base64.txt
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File -Encoding ASCII keystore.base64.txt
```

- [ ] Base64 file created
- [ ] Copy contents to clipboard

### ✅ Configure GitHub Secrets

Go to: **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add these 4 secrets:

- [ ] `KEYSTORE_BASE64` = (paste base64 string)
- [ ] `KEYSTORE_PASSWORD` = (your keystore password)
- [ ] `KEY_ALIAS` = (your key alias, e.g., `pdfx-release`)
- [ ] `KEY_PASSWORD` = (your key password)

**Verify:**
- [ ] All 4 secrets show in list (values hidden)
- [ ] No typos in secret names (case-sensitive!)

### ✅ Test Release Build

**Manual workflow trigger:**
1. [ ] Go to **Actions** tab
2. [ ] Click **Build APK** workflow
3. [ ] Click **Run workflow**
4. [ ] Select `release` from dropdown
5. [ ] Click **Run workflow**
6. [ ] Wait ~10 minutes
7. [ ] Download artifact
8. [ ] Verify APK is signed

**Check signing:**
```bash
# Linux/Mac
jarsigner -verify -verbose -certs app.apk

# Look for: "jar verified"
```

### ✅ Create Tagged Release

For automatic release builds with GitHub Releases:

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release 1.0.0

- Feature A
- Feature B
- Bug fix C"

# Push tag
git push origin v1.0.0
```

- [ ] Tag pushed
- [ ] Workflow auto-triggered
- [ ] Build completes
- [ ] GitHub Release created
- [ ] APK attached to release
- [ ] Checksum file included

---

## Troubleshooting Checklist

### ❌ Build Fails: "Gradle wrapper validation failed"

**Fix:**
```bash
./gradlew wrapper --gradle-version=8.7
git add gradle/wrapper/
git commit -m "Update Gradle wrapper"
git push
```

- [ ] Wrapper updated
- [ ] New build succeeds

### ❌ Build Fails: "SDK not found"

**Cause:** `local.properties` committed to git.

**Fix:**
```bash
git rm --cached local.properties
git commit -m "Remove local.properties from git"
git push
```

- [ ] File removed from git
- [ ] In `.gitignore`
- [ ] Build succeeds

### ❌ Build Fails: "Could not resolve dependencies"

**Cause:** Network issue or wrong dependency version.

**Check:**
- [ ] Dependencies in `build.gradle.kts` are valid
- [ ] Version numbers exist on Maven Central
- [ ] No private repositories required

**Fix:** Update to stable versions, push again.

### ❌ Release Build is Unsigned

**Cause:** Missing or incorrect GitHub secrets.

**Verify:**
- [ ] All 4 secrets exist in GitHub
- [ ] Secret names are exact (case-sensitive)
- [ ] Base64 string has no line breaks
- [ ] Passwords are correct

**Test locally:**
```bash
export KEYSTORE_FILE="./release-keystore.jks"
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="your_alias"
export KEY_PASSWORD="your_password"

./gradlew assembleRelease
```

If local build works but GitHub doesn't → secret values wrong.

### ❌ APK Size Too Large

**Expected:**
- Debug: 8-10 MB
- Release: ~6 MB

**If larger:**
- [ ] ProGuard enabled in release build
- [ ] `minifyEnabled = true`
- [ ] `shrinkResources = true`
- [ ] No unnecessary assets in `res/` or `assets/`

### ❌ Workflow Doesn't Trigger

**Check:**
- [ ] Push to `main` or `master` branch
- [ ] File changes not in `paths-ignore`
- [ ] Workflows enabled (Settings → Actions → Enable)

**Manual trigger:**
- [ ] Go to Actions → select workflow → Run workflow

---

## Build Time Expectations

| Workflow | First Build | Cached Build |
|----------|-------------|--------------|
| Debug APK | 8-12 min | 3-5 min |
| Release APK | 10-15 min | 4-6 min |
| PR Checks | 6-10 min | 3-5 min |

**If slower:**
- [ ] Check GitHub Actions quota
- [ ] Review runner status page
- [ ] Consider self-hosted runner (advanced)

---

## Post-Build Verification

### ✅ APK Downloaded

- [ ] Extract from artifact ZIP
- [ ] File size reasonable (~6-10 MB)
- [ ] Filename correct

### ✅ APK Installation

**On device/emulator:**
- [ ] Enable "Install from Unknown Sources"
- [ ] Transfer APK to device
- [ ] Install APK
- [ ] Launch app
- [ ] No crashes on startup

### ✅ App Functionality

- [ ] Splash screen shows
- [ ] Home screen loads
- [ ] Can import PDF
- [ ] Can open PDF
- [ ] Reader works
- [ ] Settings accessible
- [ ] No critical bugs

### ✅ Release Verification

**For production builds:**
- [ ] APK is signed (verified with jarsigner)
- [ ] SHA256 checksum matches
- [ ] Version number correct
- [ ] No debug logging appears
- [ ] ProGuard applied (smaller size)

---

## Continuous Integration Best Practices

### ✅ Before Every Push

- [ ] Run local build: `./gradlew assembleDebug`
- [ ] Fix any warnings
- [ ] Commit with meaningful message
- [ ] Push to feature branch first (optional)

### ✅ Pull Request Workflow

- [ ] Create PR instead of direct push
- [ ] Wait for PR checks to pass
- [ ] Review build logs
- [ ] Merge only if green ✅

### ✅ Version Management

- [ ] Update `versionCode` in `build.gradle.kts` for each release
- [ ] Update `versionName` with semantic versioning
- [ ] Tag releases: `v1.0.0`, `v1.0.1`, etc.
- [ ] Maintain CHANGELOG.md

### ✅ Artifact Management

- [ ] Debug APKs: test builds, expire after 30 days
- [ ] Release APKs: production, keep 90 days
- [ ] GitHub Releases: permanent, for distribution

---

## Quick Reference Commands

### Local Testing
```bash
# Clean build
./gradlew clean

# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Run verification script
bash verify-build.sh        # Linux/Mac
verify-build.bat            # Windows

# Check what would be committed
git status
```

### Git Operations
```bash
# Initial push
git add .
git commit -m "Initial commit"
git push -u origin main

# Tag release
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# Remove file from git (keep locally)
git rm --cached filename
```

### Keystore Operations
```bash
# Generate keystore
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pdfx-release

# View keystore info
keytool -list -v -keystore release-keystore.jks

# Verify APK signature
jarsigner -verify -verbose -certs app.apk
```

### Base64 Encoding
```bash
# Linux/Mac
base64 -i release-keystore.jks | tr -d '\n' > keystore.base64.txt

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File -Encoding ASCII keystore.base64.txt
```

---

## Final Checklist Before Going Live

### ✅ Code Quality

- [ ] All TODOs resolved
- [ ] No debug logging in production
- [ ] No hardcoded secrets
- [ ] Comments are accurate
- [ ] README.md is complete

### ✅ Security

- [ ] No sensitive files in git
- [ ] API keys in secrets (if any)
- [ ] Signing configured properly
- [ ] No internet permission (unless needed)

### ✅ Build System

- [ ] All workflows pass
- [ ] Debug builds work
- [ ] Release builds are signed
- [ ] APK size is reasonable
- [ ] No build warnings

### ✅ Testing

- [ ] App installs on Android 8.0+
- [ ] Core features work
- [ ] No crashes on startup
- [ ] UI renders correctly
- [ ] Performance is smooth

### ✅ Distribution

- [ ] GitHub Releases configured
- [ ] APK can be downloaded
- [ ] Installation instructions clear
- [ ] License file included

---

## Success Criteria

Your GitHub Actions setup is complete when:

✅ Push to main → APK builds automatically  
✅ Download artifact → APK installs  
✅ Launch app → Works perfectly  
✅ Create tag → Release created with APK  
✅ No manual intervention needed  

**All checks pass → You're ready to go! 🎉**
