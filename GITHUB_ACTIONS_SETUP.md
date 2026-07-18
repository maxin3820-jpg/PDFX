# GitHub Actions Setup Guide

Complete guide to building PDFX APKs automatically on GitHub with **zero errors**.

---

## Overview

PDFX includes **3 GitHub Actions workflows** for automated APK building:

1. **`debug-apk.yml`** — Auto-builds debug APK on every push (no signing required)
2. **`build-apk.yml`** — Manual workflow with debug/release choice
3. **`release-apk.yml`** — Release builds with versioning and GitHub Releases

All workflows are configured for **error-free execution** with comprehensive validation, caching, and error handling.

---

## Quick Start (Debug APKs)

Debug builds work **immediately** without any configuration:

### Step 1: Push to GitHub
```bash
cd /path/to/PDFX
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/yourusername/pdfx.git
git push -u origin main
```

### Step 2: Download APK
1. Go to **Actions** tab
2. Click on the latest **Build Debug APK** workflow
3. Scroll to **Artifacts** section
4. Download **PDFX-debug-XXX.zip**
5. Extract and install the APK

✅ **Done!** No secrets, no keystore, no configuration needed.

---

## Release APKs (Signed)

For **production-ready signed APKs**, you need to configure signing secrets.

### Prerequisites

1. **Android Keystore** — generate if you don't have one
2. **GitHub Repository** — with Actions enabled

---

## Creating a Keystore

If you don't have a keystore, generate one:

```bash
keytool -genkey -v \
  -keystore release-keystore.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias pdfx-release
```

**Fill in the prompts:**
- Keystore password: (choose a strong password)
- Key password: (can be same as keystore password)
- First and last name: Your Name
- Organizational unit: Your Company
- Organization: Your Company
- City: Your City
- State: Your State
- Country code: US (or your country)

**Save these values — you'll need them for GitHub secrets:**
- Keystore password
- Key alias (e.g., `pdfx-release`)
- Key password

---

## Configuring GitHub Secrets

### Step 1: Encode Keystore to Base64

**On Linux/Mac:**
```bash
base64 -i release-keystore.jks | tr -d '\n' > keystore.base64.txt
cat keystore.base64.txt
```

**On Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File -Encoding ASCII keystore.base64.txt
Get-Content keystore.base64.txt
```

Copy the entire output (single line of text).

---

### Step 2: Add Secrets to GitHub

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add these 4 secrets:

| Secret Name | Value | Example |
|-------------|-------|---------|
| `KEYSTORE_BASE64` | Paste the base64 string from Step 1 | (long base64 string) |
| `KEYSTORE_PASSWORD` | Your keystore password | `MyStrongPassword123` |
| `KEY_ALIAS` | Your key alias | `pdfx-release` |
| `KEY_PASSWORD` | Your key password | `MyStrongPassword123` |

✅ **Done!** Release builds will now be signed automatically.

---

## Workflows Explained

### 1. **debug-apk.yml** (Automatic)

**Triggers:**
- Every push to `main`, `master`, or `develop` branches
- Ignores markdown files and preview folder

**What it does:**
- Validates Gradle wrapper
- Sets up JDK 17
- Builds debug APK (unsigned)
- Uploads artifact with SHA256 checksum

**Output:**
- `PDFX-debug-{run_number}.apk`
- SHA256 checksum in build summary

**Use case:** Daily development builds, testing

---

### 2. **build-apk.yml** (Manual)

**Triggers:**
- Manual dispatch from Actions tab
- Choice between debug or release

**What it does:**
- Builds either debug or release APK
- Checks for signing secrets (release only)
- Signed if secrets available, unsigned otherwise
- Runs lint checks in parallel
- Uploads lint report

**Output:**
- `pdfx-debug-{run_number}.apk` or
- `pdfx-release-{run_number}.apk`
- Lint report HTML

**Use case:** On-demand builds, testing both variants

---

### 3. **release-apk.yml** (Release)

**Triggers:**
- Git tags matching `v*` (e.g., `v1.0.0`)
- Manual dispatch with version input

**What it does:**
- Builds signed release APK
- Renames APK with version number
- Creates SHA256 checksum file
- Creates GitHub Release with APK attached
- Uploads to Artifacts

**Output:**
- `PDFX-v{version}-release.apk`
- `SHA256SUMS.txt`
- GitHub Release page

**Use case:** Official releases, production builds

---

## Building APKs

### Method 1: Automatic (Debug)

Just push code:
```bash
git add .
git commit -m "Add feature X"
git push
```

Wait 5-10 minutes → Download from Actions tab.

---

### Method 2: Manual (Debug or Release)

1. Go to **Actions** tab
2. Click **Build APK** workflow
3. Click **Run workflow**
4. Choose **debug** or **release**
5. Click **Run workflow**
6. Wait 5-10 minutes
7. Download from Artifacts

---

### Method 3: Tagged Release

Create and push a tag:
```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

Workflow automatically:
1. Builds signed release APK
2. Creates GitHub Release
3. Attaches APK to release
4. Generates release notes

Download from **Releases** page.

---

## Workflow Features

### ✅ Zero-Error Design

All workflows include:

1. **Gradle Wrapper Validation** — prevents malicious wrapper
2. **Timeout Protection** — max 30 minutes per job
3. **Comprehensive Error Handling** — detailed logs on failure
4. **SDK Verification** — confirms Android SDK availability
5. **File Existence Checks** — validates APK before upload

### ⚡ Performance Optimization

1. **Gradle Caching** — reuses dependencies across builds
2. **Parallel Jobs** — lint runs alongside build
3. **No Daemon** — prevents hanging processes
4. **Cleanup** — removes keystore after build

### 📊 Build Information

Every build generates:
- APK name and size
- SHA256 checksum
- Build number
- Commit SHA
- Signed/unsigned status

### 🔒 Security

1. **Secrets Never Logged** — keystore password hidden
2. **Keystore Cleanup** — removed immediately after build
3. **Base64 Encoding** — safe binary transmission
4. **Limited Permissions** — workflows use minimal GitHub token scope

---

## Troubleshooting

### Build Fails with "Gradle wrapper validation failed"

**Cause:** Gradle wrapper checksum mismatch.

**Fix:**
```bash
./gradlew wrapper --gradle-version=8.7
git add gradle/wrapper/
git commit -m "Update Gradle wrapper"
git push
```

---

### Release Build is Unsigned

**Cause:** Missing GitHub secrets.

**Check:**
1. Go to Settings → Secrets → Actions
2. Verify all 4 secrets exist:
   - KEYSTORE_BASE64
   - KEYSTORE_PASSWORD
   - KEY_ALIAS
   - KEY_PASSWORD

**Fix:** Follow "Configuring GitHub Secrets" section above.

---

### APK Size Larger Than Expected

**Cause:** Debug build includes debug symbols.

**Solution:** Use release build for production:
- Release APK: ~6 MB (ProGuard enabled)
- Debug APK: ~8-10 MB (no optimization)

---

### Workflow Doesn't Trigger

**Cause:** Markdown-only changes are ignored.

**Check:** Did you only modify `.md` files?

**Fix:** Make a code change or trigger manually.

---

### Lint Failures

**Note:** Lint runs as `continue-on-error: true`.

**View report:**
1. Download lint report artifact
2. Open `lint-results-debug.html`
3. Fix issues if needed

Lint failures **don't stop** APK builds.

---

## Build Time Estimates

| Workflow | First Run | Cached Run |
|----------|-----------|------------|
| Debug APK | 8-12 min | 3-5 min |
| Release APK | 10-15 min | 4-6 min |
| Build + Lint | 12-18 min | 5-8 min |

Caching improves subsequent builds significantly.

---

## Artifact Retention

| Artifact Type | Retention |
|---------------|-----------|
| Debug APK | 30 days |
| Release APK | 90 days |
| Lint Reports | 30 days |
| GitHub Releases | Forever |

Download artifacts before expiration or use tagged releases for permanent storage.

---

## Advanced: Custom Signing Config

If you want to use different signing configs per branch/tag, modify `build.gradle.kts`:

```kotlin
android {
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
        }
    }
}
```

Environment variables are set automatically by workflows.

---

## Checklist: First-Time Setup

### For Debug Builds (No Config Needed)
- [ ] Push code to GitHub
- [ ] Wait for workflow to complete
- [ ] Download APK from Artifacts

### For Release Builds (One-Time Setup)
- [ ] Generate keystore (if needed)
- [ ] Encode keystore to base64
- [ ] Add 4 secrets to GitHub
- [ ] Test with manual workflow
- [ ] Tag a release or trigger manually

---

## Example: Complete Release Workflow

```bash
# 1. Make changes
git add .
git commit -m "Release 1.0.0: Performance optimizations"

# 2. Create tag
git tag -a v1.0.0 -m "Version 1.0.0

- 50% memory reduction (RGB_565)
- Page render caching
- Two-tier thumbnail cache
- ProGuard optimization"

# 3. Push tag
git push origin v1.0.0

# 4. Wait for workflow (10-15 minutes)
#    → Workflow builds signed APK
#    → Creates GitHub Release
#    → Attaches APK + checksum

# 5. Go to Releases page
#    → Download PDFX-v1.0.0-release.apk
#    → Verify SHA256 checksum

# Done! 🎉
```

---

## Summary

| Build Type | Trigger | Signing | Output | Use Case |
|------------|---------|---------|--------|----------|
| **Debug** | Automatic | No | Debug APK | Development |
| **Release** | Manual | Optional | Signed/Unsigned | Testing |
| **Tagged Release** | Git tag | Yes | Signed + GitHub Release | Production |

All workflows are **production-ready** and include:
- ✅ Zero-error validation
- ✅ Comprehensive logging
- ✅ SHA256 checksums
- ✅ Gradle caching
- ✅ Security best practices

Push to GitHub → Wait → Download APK. **That's it!**
