# GitHub Actions Workflows

This directory contains automated build workflows for PDFX.

---

## Available Workflows

### 1. **Build Debug APK** (`debug-apk.yml`)

**Purpose:** Automatically builds debug APKs on every push.

**Triggers:**
- Push to `main`, `master`, or `develop` branches
- Manual trigger via "Run workflow" button

**Output:**
- Debug APK (unsigned)
- Artifact name: `PDFX-debug-{run_number}`
- Retention: 30 days

**No setup required** — works out of the box!

---

### 2. **Build APK** (`build-apk.yml`)

**Purpose:** Full-featured workflow supporting both debug and release builds with optional signing.

**Triggers:**
- Push to `main` or `master`
- Pull requests
- Manual trigger (can choose debug or release)

**Features:**
- ✅ Debug builds (no signing needed)
- ✅ Release builds (signed if secrets configured)
- ✅ Lint checks in parallel
- ✅ Automatic artifact upload
- ✅ Build summary with APK size

**Setup required for signed releases:** See [SIGNING_SETUP.md](./SIGNING_SETUP.md)

---

## Quick Start

### Option 1: Just Build Debug APKs (Easiest)

1. Push your code to GitHub:
   ```bash
   git add .
   git commit -m "Initial commit"
   git push origin main
   ```

2. Go to **Actions** tab in your repository

3. Click on the **Build Debug APK** workflow run

4. Wait for the build to complete (~3-5 minutes)

5. Download the APK from **Artifacts** section

6. Install on your Android device

**Done!** No configuration needed.

---

### Option 2: Build Signed Release APKs

Follow the comprehensive guide: [SIGNING_SETUP.md](./SIGNING_SETUP.md)

**Summary:**
1. Create a keystore file
2. Encode it to Base64
3. Add 4 secrets to GitHub
4. Trigger the workflow
5. Download signed release APK

---

## Workflow Requirements

Both workflows require:
- ✅ **JDK 17** (automatically installed by GitHub Actions)
- ✅ **Android SDK** (pre-installed on `ubuntu-latest` runners)
- ✅ **Gradle** (wrapper included in repository)

No manual setup needed — everything is automated!

---

## How to Trigger Workflows Manually

### Via GitHub Web UI:

1. Go to your repository
2. Click **Actions** tab
3. Select the workflow (left sidebar)
4. Click **Run workflow** (right side)
5. Choose branch and options
6. Click green **Run workflow** button

### Via GitHub CLI:

```bash
# Build debug APK
gh workflow run debug-apk.yml

# Build release APK (if signing is configured)
gh workflow run build-apk.yml -f build_type=release
```

---

## Understanding Build Artifacts

After a successful build, artifacts are uploaded and can be downloaded:

### Debug APK
- **Name:** `PDFX-debug-{run_number}.apk`
- **Size:** ~8 MB
- **Signed:** Yes (with Android debug keystore)
- **Installable:** Yes (allow "Install from unknown sources")

### Release APK (unsigned)
- **Name:** `PDFX-release-unsigned-{run_number}.apk`
- **Size:** ~6 MB (ProGuard optimized)
- **Signed:** No
- **Installable:** Must be signed first

### Release APK (signed)
- **Name:** `PDFX-release-{run_number}.apk`
- **Size:** ~6 MB
- **Signed:** Yes (with your release keystore)
- **Installable:** Yes (production-ready)

---

## Build Times

Typical build times on GitHub Actions runners:

| Task | Time |
|------|------|
| Checkout + Setup | ~30s |
| Gradle sync | ~1m |
| Compile debug | ~2m |
| Compile release | ~3m (ProGuard optimization) |
| Lint checks | ~1m |
| **Total (debug)** | **~3-4 minutes** |
| **Total (release)** | **~4-5 minutes** |

---

## Troubleshooting

### Build fails with "SDK not found"
**Fix:** This shouldn't happen — SDK is pre-installed. If it does, check `local.properties` creation step.

### Build fails with "Execution failed for task ':app:lintDebug'"
**Fix:** Lint errors don't fail the build (continue-on-error). Check the lint report artifact.

### Artifact download is a ZIP file
**Expected:** GitHub packages artifacts as ZIP. Extract to get the APK.

### APK won't install on device
**Possible causes:**
- Release APK is unsigned → sign it first
- Device security settings → enable "Install from unknown sources"
- Conflicting package → uninstall old version first

---

## Advanced: Customizing Workflows

### Change retention period:
```yaml
- name: Upload APK artifact
  uses: actions/upload-artifact@v4
  with:
    retention-days: 60  # Change from 30 to 60 days
```

### Add version to artifact name:
```yaml
- name: Get version
  id: version
  run: |
    VERSION=$(grep 'versionName' app/build.gradle.kts | cut -d'"' -f2)
    echo "name=$VERSION" >> $GITHUB_OUTPUT

- name: Upload APK
  with:
    name: PDFX-v${{ steps.version.outputs.name }}-debug
```

### Build on tag push only:
```yaml
on:
  push:
    tags:
      - 'v*'
```

### Send notification on build complete:
Add this step at the end:
```yaml
- name: Notify
  uses: someorg/notify-action@v1
  with:
    webhook: ${{ secrets.WEBHOOK_URL }}
```

---

## Security Notes

✅ **Secrets are encrypted** at rest by GitHub  
✅ **Secrets are masked** in logs (never visible)  
✅ **Keystore is temporary** — created and deleted during build  
✅ **No secrets in artifacts** — only the signed APK is uploaded  

❌ **Never commit** keystore files to Git  
❌ **Never share** GitHub Secrets in plain text  
❌ **Never use** weak passwords for keystores  

---

## Monitoring Builds

### Via Email:
GitHub automatically sends emails on build failures (if enabled in settings).

### Via GitHub Mobile App:
Get push notifications for Actions workflow runs.

### Via Status Badge:
Add to your README.md:
```markdown
![Build Status](https://github.com/username/pdfx/actions/workflows/debug-apk.yml/badge.svg)
```

---

## Cost

GitHub Actions is **free** for public repositories:
- ✅ Unlimited minutes
- ✅ Unlimited storage (with retention limits)
- ✅ Concurrent builds

For private repositories:
- ✅ 2,000 minutes/month free (free tier)
- ✅ ~3-5 minutes per build
- ✅ = ~400-600 builds/month free

**PDFX build costs:** ~5 minutes × 2 jobs = **10 minutes per workflow run**

---

## Summary

- ✅ **Two workflows:** Simple debug + Full build
- ✅ **Zero setup for debug:** Push and build automatically
- ✅ **Optional signing for release:** Add 4 secrets
- ✅ **Fast builds:** 3-5 minutes
- ✅ **Artifacts retained:** 30-90 days
- ✅ **Secure:** No secrets in logs or artifacts

**Next steps:**
1. Push code to GitHub
2. Check Actions tab
3. Download your APK
4. Install and test

For signing setup, see: [SIGNING_SETUP.md](./SIGNING_SETUP.md)
