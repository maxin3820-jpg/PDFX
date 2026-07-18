# PDFX

**The cleanest PDF reading experience on Android.**

PDFX is a minimal, fast, and distraction-free PDF reader built on modern Android technologies. The philosophy is simple:

> Open App → Import PDF → Tap PDF → Read

No ads. No account. No cloud sync. No tracking. Everything stays on your device.

---

## Screenshots

_Coming soon._

---

## Philosophy

PDFX is not trying to compete with Adobe Acrobat. It exists to be the fastest, cleanest PDF reading experience on Android. Every design decision is made in service of that single goal.

**What PDFX is:**
- A beautiful, minimal PDF library
- A fast, smooth reader with vertical scroll and pinch-to-zoom
- A privacy-first app with zero network access

**What PDFX is not:**
- An editor, annotator, or scanner
- A cloud-connected app
- A feature-bloated power tool

---

## Features

- **Instant open** — tap a PDF, it opens. No loading screens.
- **Two-column grid library** with lazily generated thumbnails
- **Persistent library** via Android Storage Access Framework (SAF) — never modifies your files
- **Smooth vertical scroll** with pinch-to-zoom and double-tap zoom
- **Reading position memory** — restores page, zoom, and scroll position
- **Temporary opens** — open PDFs from WhatsApp, Gmail, etc. without adding to library
- **Rename & remove** — display name only, original file never touched
- **Stale entry cleanup** — auto-removes library entries if a file is deleted outside PDFX
- **10 accent colours** — Blue, Indigo, Purple, Pink, Red, Orange, Amber, Green, Teal, Slate
- **12 reader backgrounds** — Light, Sepia, Warm Sepia, Rose, Dark, Dark Grey, Dark Blue, AMOLED, etc.
- **5 card styles** — Elevated, Flat, Filled, Outlined, Compact
- **Material 3** design with dynamic colour on Android 12+
- **No internet permission**

---

## Performance Optimizations

PDFX is built for **speed and smoothness**:

### Memory Management
- **RGB_565 bitmaps** — 50% less memory than ARGB_8888 for PDFs (no alpha channel needed)
- **Two-tier thumbnail cache** — LRU memory cache (1/6 heap) + JPEG disk cache
- **Page-level render cache** — rendered pages are cached to avoid redundant PdfRenderer calls
- **Aggressive JPEG compression** — quality 80 for smaller disk footprint and faster loads

### UI Performance
- **LazyColumn with item keys** — efficient recycling and composition
- **Content type hints** — helps Compose optimize recomposition
- **Strong skipping mode** — Compose compiler optimization enabled
- **Hardware acceleration** — enabled by default in manifest
- **singleTask launch mode** — prevents duplicate activity instances

### Build Optimizations
- **ProGuard** — aggressive optimization with 5 passes in release builds
- **Resource shrinking** — removes unused resources automatically
- **Debug symbol level** — optimized for release builds
- **Log removal** — all Log.d/v/i calls stripped in release

### Threading
- **All I/O on Dispatchers.IO** — file reads, PDF rendering, thumbnail generation
- **Debounced persistence** — reading position saved after 500ms idle
- **Concurrent operations** — parallel thumbnail generation for imports

PDFX handles **1000+ library documents** with smooth scrolling and instant opens.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository pattern |
| PDF Rendering | Android `PdfRenderer` (built-in, no native libs) |
| Database | Room |
| Settings | DataStore (Preferences) |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Image loading | Coil (thumbnail cache) |
| Build | Gradle Kotlin DSL + Version Catalogs |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |

---

## Project Structure

```
app/
├── data/
│   ├── database/       # Room entity, DAO, database, mapper
│   └── repository/     # Repository implementations
├── di/                 # Hilt modules
├── domain/
│   ├── model/          # Pure domain models (PdfDocument, AppSettings, …)
│   └── repository/     # Repository interfaces
├── navigation/         # NavRoutes + PdfxNavGraph
├── ui/
│   ├── components/     # Reusable composables (PdfCard, dialogs, …)
│   ├── home/           # Library screen
│   ├── reader/         # PDF reader screen
│   ├── settings/       # Settings screen
│   └── theme/          # Material 3 colour, type, theme
├── utils/              # PdfUtils, ThumbnailCache, UriPermissionHelper
└── viewmodel/          # HomeViewModel, ReaderViewModel, SettingsViewModel, MainViewModel
```

---

## Building

### Option 1: GitHub Actions (Easiest — No Local Setup Required)

Build APKs automatically in the cloud with **zero-error workflows**:

#### Debug Builds (No Configuration)
```bash
git push origin main
# Wait 5-10 minutes → Download from Actions → Artifacts
```

#### Release Builds (One-Time Setup)
```bash
# 1. Add 4 secrets to GitHub (keystore, passwords)
# 2. Push tag:
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
# 3. Download signed APK from Releases page
```

**Features:**
- ✅ Automatic builds on every push
- ✅ Debug APKs (no config needed)
- ✅ Signed release APKs (with secrets)
- ✅ SHA256 checksums
- ✅ GitHub Releases integration
- ✅ Pre-push verification scripts

**Complete guides:**
- [`GITHUB_ACTIONS_SETUP.md`](./GITHUB_ACTIONS_SETUP.md) — Full setup guide
- [`GITHUB_BUILD_CHECKLIST.md`](./GITHUB_BUILD_CHECKLIST.md) — Pre-push checklist
- [`GITHUB_CI_COMPLETE.md`](./GITHUB_CI_COMPLETE.md) — Overview & summary

**Verification scripts:**
```bash
bash verify-build.sh    # Linux/Mac
verify-build.bat        # Windows
```

---

### Option 2: Local Build

#### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

#### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourname/pdfx.git
   cd pdfx
   ```

2. Open in Android Studio.

3. Let Gradle sync complete.

4. Run on a device or emulator running Android 8.0+ (API 26+):
   ```bash
   ./gradlew installDebug
   ```

5. Build a release APK:
   ```bash
   ./gradlew assembleRelease
   ```
   > You will need to configure a signing config in `app/build.gradle.kts` before releasing.

**Detailed instructions:** [BUILD_INSTRUCTIONS.md](./BUILD_INSTRUCTIONS.md)

---

## Privacy

PDFX has **no internet permission**. It cannot make network requests. No analytics, no telemetry, no crash reporting. All data is stored locally on the device using Room and DataStore.

SAF persistent URI permissions are used to read PDF files. The original files are **never modified or deleted** by PDFX.

---

## Permissions

| Permission | Why |
|---|---|
| `READ_EXTERNAL_STORAGE` (API ≤ 32 only) | Required to read PDFs on older Android versions. Not requested on API 33+. |

No other permissions are requested or used.

---

## License

```
MIT License

Copyright (c) 2024 PDFX Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
