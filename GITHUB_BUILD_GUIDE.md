# Building PDFX APK via GitHub Actions

**Quick guide to automatically build APKs using GitHub Actions — zero local setup required.**

---

## Why Use GitHub Actions?

✅ **No Android Studio needed** — builds in the cloud  
✅ **No local Java/SDK setup** — everything is automated  
✅ **Build from any device** — even your phone (via GitHub Mobile)  
✅ **Always uses correct dependencies** — clean environment every time  
✅ **Free for public repos** — unlimited builds  

---

## Method 1: Automatic Debug Builds (Recommended)

### Setup (One-time)

1. **Push your code to GitHub:**
   ```bash
   cd c:\Users\zc\Desktop\PDFX
   git init
   git add .
   git commit -m "Initial PDFX commit"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/PDFX.git
   git push -u origin main
   ```

2. **That's it!** The build starts automatically.

### Download APK

1. Go to your repository on GitHub
2. Click **Actions** tab (top menu)
3. Click on the latest workflow run (green checkmark = success)
4. Scroll down to **Artifacts** section
5. Click **PDFX-debug-{number}** to download
6. Extract the ZIP file
7. Transfer APK to your Android device
8. Install (enable "Install from unknown sources" if needed)

### Future Builds

Every time you push code, a new APK is automatically built!

```bash
# Make changes to code
git add .
git commit -m "Added feature X"
git push

# Wait 3-4 minutes, then download from Actions tab
```

---

## Method 2: Manual Trigger

If you want to build without pushing code:

1. Go to **Actions** tab
2. Click **Build Debug APK** (left sidebar)
3. Click **Run workflow** button (right side)
4. Select branch: `main`
5. Click green **Run workflow** button
6. Wait ~3-4 minutes
7. Download from Artifacts section

---

## Method 3: Signed Release Builds

For production-ready, signed APKs:

### Requirements:
- Release keystore file (see instructions below)
- 4 GitHub Secrets configured

### Quick Setup:

#### Step 1: Create Keystore (if you don't have one)

**Windows (PowerShell):**
```powershell
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pdfx-release
```

**Linux/Mac:**
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pdfx-release
```

**Enter when prompted:**
- Keystore password: (create a strong password)
- Key password: (same as keystore password, or different)
- Name, organization, etc.: (fill as you like)

**⚠️ Important:** Save this keystore file safely! You'll need it for all future app updates.

#### Step 2: Convert to Base64

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks")) | Out-File keystore.txt
```

**Linux/Mac:**
```bash
base64 -i release-keystore.jks -o keystore.txt
```

**Windows (Git Bash):**
```bash
base64 -w 0 release-keystore.jks > keystore.txt
```

#### Step 3: Add GitHub Secrets

1. Go to your repository on GitHub
2. Click **Settings** (top menu)
3. Click **Secrets and variables** → **Actions** (left sidebar)
4. Click **New repository secret** (green button)

Add these 4 secrets:

| Name | Value |
|------|-------|
| `KEYSTORE_BASE64` | Contents of `keystore.txt` file (entire text) |
| `KEYSTORE_PASSWORD` | Password you entered when creating keystore |
| `KEY_ALIAS` | `pdfx-release` (or whatever you used) |
| `KEY_PASSWORD` | Key password (usually same as keystore password) |

#### Step 4: Build Signed Release

1. Go to **Actions** tab
2. Click **Build APK** workflow (left sidebar)
3. Click **Run workflow**
4. Select `release` from dropdown
5. Click **Run workflow**
6. Wait ~4-5 minutes
7. Download signed APK from Artifacts

**Result:** Production-ready signed APK (~6 MB, optimized with ProGuard)

---

## Workflow Status

### How to know if build succeeded:

✅ **Green checkmark** = Build successful  
❌ **Red X** = Build failed  
🟡 **Yellow dot** = Build in progress  

Click on the workflow run to see detailed logs.

---

## APK Comparison

| Type | Size | Signed | Use Case | How to Get |
|------|------|--------|----------|------------|
| **Debug** | ~8 MB | Yes (debug key) | Testing, development | Automatic on push |
| **Release (unsigned)** | ~6 MB | No | Manual signing later | Manual trigger |
| **Release (signed)** | ~6 MB | Yes (your key) | Production, Play Store | Manual trigger + secrets |

---

## Common Questions

### Q: Do I need Android Studio?
**A:** No! GitHub Actions builds everything in the cloud.

### Q: Do I need Java/Android SDK installed?
**A:** No! The workflow installs everything automatically.

### Q: How long does a build take?
**A:** 3-4 minutes for debug, 4-5 minutes for release.

### Q: Can I build from my phone?
**A:** Yes! Use GitHub Mobile app → Actions → Run workflow.

### Q: How many builds can I do?
**A:** Unlimited for public repos. 2,000 minutes/month free for private repos (~200 builds).

### Q: Where is the APK stored?
**A:** In the **Artifacts** section of each workflow run, retained for 30-90 days.

### Q: Can I build without pushing code?
**A:** Yes! Use "Run workflow" button in Actions tab (manual trigger).

### Q: Do I need to configure signing for debug builds?
**A:** No! Debug builds work out-of-the-box with no setup.

---

## Troubleshooting

### Build Failed

**Check the logs:**
1. Go to Actions tab
2. Click on the failed workflow run
3. Click on the job (e.g., "Build Android APK")
4. Read the error message

**Common issues:**
- **Gradle sync failed:** Usually means a dependency issue. Check `build.gradle.kts`.
- **Keystore error:** Wrong password in secrets or corrupted Base64 encoding.
- **Lint errors:** Don't fail the build — just warnings. Check lint report artifact.

### Can't Find Artifacts

**Make sure:**
- Build completed successfully (green checkmark)
- You're scrolling down to "Artifacts" section (below job logs)
- Artifact hasn't expired (30-90 day retention)

### APK Won't Install

**Possible causes:**
- Release APK is unsigned → Configure signing or sign manually
- Device blocked unknown sources → Go to Settings → Security → Allow
- Conflicting package name → Uninstall old PDFX version first

---

## Next Steps

1. ✅ Push code to GitHub
2. ✅ Check Actions tab for build status
3. ✅ Download APK from Artifacts
4. ✅ Install on Android device
5. ✅ Test the app

For signed release builds, follow the 4-step setup in Method 3 above.

---

## Additional Resources

- **Detailed signing guide:** `.github/SIGNING_SETUP.md`
- **Workflow documentation:** `.github/README.md`
- **Build instructions (local):** `BUILD_INSTRUCTIONS.md`

---

## Summary

**Easiest path:**
1. `git push` → Wait 4 minutes → Download APK → Install

**Production path:**
1. Create keystore → Encode to Base64 → Add secrets → Run workflow → Download signed APK

**Both methods work from any device with zero local Android development setup required.**

Happy building! 🚀
