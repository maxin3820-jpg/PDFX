# GitHub Actions Pre-Flight Checklist

Use this checklist before pushing to GitHub to ensure builds will succeed.

---

## ✅ Repository Setup

- [ ] Git repository initialized (`git init`)
- [ ] Remote repository created on GitHub
- [ ] Remote added locally (`git remote add origin ...`)
- [ ] `.gitignore` includes keystore files
- [ ] All files staged (`git add .`)
- [ ] Initial commit created (`git commit -m "..."`)

---

## ✅ Required Files Present

- [ ] `.github/workflows/debug-apk.yml` exists
- [ ] `.github/workflows/build-apk.yml` exists
- [ ] `gradlew` exists and is executable
- [ ] `gradlew.bat` exists (for Windows)
- [ ] `gradle/wrapper/gradle-wrapper.jar` exists
- [ ] `gradle/wrapper/gradle-wrapper.properties` exists
- [ ] `settings.gradle.kts` exists
- [ ] `build.gradle.kts` (root) exists
- [ ] `app/build.gradle.kts` exists
- [ ] `gradle/libs.versions.toml` exists

---

## ✅ Gradle Configuration

- [ ] `compileSdk = 34` in app/build.gradle.kts
- [ ] `minSdk = 26` in app/build.gradle.kts
- [ ] `targetSdk = 34` in app/build.gradle.kts
- [ ] Kotlin version compatible with Compose
- [ ] All dependencies use version catalog or explicit versions
- [ ] No local file paths in dependencies (e.g., `files("libs/...")`)
- [ ] ProGuard rules present (`proguard-rules.pro`)

---

## ✅ Source Code

- [ ] All Kotlin files compile locally (no syntax errors)
- [ ] No references to local files outside project directory
- [ ] All imports are from standard libraries or declared dependencies
- [ ] No hardcoded paths (e.g., `C:\Users\...`)
- [ ] AndroidManifest.xml is valid
- [ ] All resources exist (drawables, strings, etc.)

---

## ✅ Dependencies

- [ ] All dependencies are from Maven Central or Google Maven
- [ ] No snapshot versions (e.g., `1.0.0-SNAPSHOT`)
- [ ] No local Maven repositories in `repositories` block
- [ ] Hilt/Dagger configuration is correct
- [ ] Room schema export path is relative (`$projectDir/schemas`)

---

## ✅ Keystore & Signing (Optional — for Release Builds)

### If NOT setting up signing now:
- [ ] Understand debug builds will work automatically
- [ ] Release builds will be unsigned (can sign manually later)

### If setting up signing now:
- [ ] Keystore file created (`release-keystore.jks`)
- [ ] Keystore file NOT committed to Git (in `.gitignore`)
- [ ] Keystore converted to Base64 (`keystore.txt` created)
- [ ] GitHub Secret `KEYSTORE_BASE64` added
- [ ] GitHub Secret `KEYSTORE_PASSWORD` added
- [ ] GitHub Secret `KEY_ALIAS` added
- [ ] GitHub Secret `KEY_PASSWORD` added
- [ ] All secrets tested (no typos)

---

## ✅ GitHub Actions Workflow Files

### debug-apk.yml:
- [ ] File is in `.github/workflows/` directory
- [ ] YAML syntax is valid (no tabs, correct indentation)
- [ ] Triggers include `push` and `workflow_dispatch`
- [ ] JDK version is 17
- [ ] Gradle command is `./gradlew assembleDebug`

### build-apk.yml:
- [ ] File is in `.github/workflows/` directory
- [ ] YAML syntax is valid
- [ ] Supports both debug and release builds
- [ ] Handles missing secrets gracefully (builds unsigned)
- [ ] Artifacts are uploaded
- [ ] Keystore cleanup step is present

---

## ✅ Documentation

- [ ] README.md mentions GitHub Actions
- [ ] GITHUB_BUILD_GUIDE.md exists
- [ ] .github/SIGNING_SETUP.md exists
- [ ] .github/README.md exists
- [ ] All instructions are clear and complete

---

## ✅ Testing Locally (Optional but Recommended)

- [ ] `./gradlew clean` succeeds
- [ ] `./gradlew assembleDebug` succeeds locally
- [ ] `./gradlew lintDebug` completes (warnings OK, errors should be fixed)
- [ ] APK installs on test device
- [ ] App launches and functions correctly

---

## ✅ Final Steps Before Push

1. **Review .gitignore:**
   ```bash
   cat .gitignore
   ```
   Ensure `*.jks`, `*.keystore`, `keystore.txt` are listed.

2. **Check for sensitive files:**
   ```bash
   git status
   ```
   Make sure no keystore files are staged.

3. **Verify remote:**
   ```bash
   git remote -v
   ```
   Should show your GitHub repository URL.

4. **Push to GitHub:**
   ```bash
   git push -u origin main
   ```

5. **Monitor first build:**
   - Go to Actions tab immediately
   - Watch the workflow run in real-time
   - Check for any errors

---

## ✅ After First Build

- [ ] Workflow completed successfully (green checkmark)
- [ ] Artifact was created
- [ ] Artifact can be downloaded
- [ ] ZIP extracts correctly
- [ ] APK is inside the ZIP
- [ ] APK installs on Android device
- [ ] App launches correctly

---

## 🚨 Common Issues & Quick Fixes

### Issue: "Gradle sync failed"
**Fix:** Check `settings.gradle.kts` includes all modules:
```kotlin
include(":app")
```

### Issue: "Could not find com.android.tools.build:gradle:X.X.X"
**Fix:** Verify `gradle/libs.versions.toml` or root `build.gradle.kts` has correct AGP version.

### Issue: "Execution failed for task ':app:lintDebug'"
**Fix:** Lint doesn't fail builds (continue-on-error). Download lint report to see warnings.

### Issue: "Keystore was tampered with"
**Fix:** 
1. Re-encode keystore to Base64 (ensure no extra whitespace)
2. Update `KEYSTORE_BASE64` secret
3. Verify passwords in secrets match keystore

### Issue: "Permission denied: ./gradlew"
**Fix:** This shouldn't happen (workflow runs `chmod +x gradlew`). If it does, commit gradlew with execute permission:
```bash
git update-index --chmod=+x gradlew
git commit -m "Make gradlew executable"
git push
```

---

## 🎯 Success Criteria

Your GitHub Actions setup is ready when:

✅ Push code → Build starts automatically  
✅ Build completes in 3-5 minutes  
✅ Green checkmark appears  
✅ APK downloads from Artifacts  
✅ APK installs on device  
✅ App runs without crashes  

---

## 📊 Build Statistics

After your first successful build, you should see:

- **Build time:** 3-4 minutes (debug), 4-5 minutes (release)
- **APK size:** ~8 MB (debug), ~6 MB (release)
- **Artifact retention:** 30 days (debug), 90 days (release)
- **Minutes used:** ~5 minutes per build (free tier: 2,000 min/month)

---

## 📝 Notes

- **First build is slower** — Gradle downloads dependencies (~1-2 min extra)
- **Subsequent builds are faster** — dependencies are cached
- **Parallel jobs** — Debug build + Lint run simultaneously (faster)
- **Automatic cleanup** — Temporary files are cleaned after each build

---

## ✨ Optional Enhancements

After basic setup works, consider:

- [ ] Add status badge to README
- [ ] Set up branch protection rules
- [ ] Enable automatic dependency updates (Dependabot)
- [ ] Add workflow to create GitHub releases
- [ ] Configure notifications (email, Slack, etc.)
- [ ] Add workflow to deploy to Play Store (for production)

---

## 🎉 Ready to Go!

If all items are checked, you're ready to push:

```bash
git push -u origin main
```

Then head to the **Actions** tab and watch your first build! 🚀

---

## Support

If you encounter issues:
1. Check workflow logs in Actions tab
2. Review this checklist
3. Read `.github/README.md` for detailed docs
4. See `GITHUB_BUILD_GUIDE.md` for troubleshooting

Good luck! 🍀
