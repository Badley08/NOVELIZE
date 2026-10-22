# Changelog - NoveLize

All notable changes to this project are documented in this file.
This changelog contains no emojis in accordance with project versioning standards.

## [1.5] - 2026-10-21

### Optimizations and Stability
- On-demand AI voice and audio drama initialization: neural voice models (Piper), SoundPool (50 SFX), and AudioTrack buffers are no longer instantiated at application launch, but loaded strictly upon opening the Reader screen to eliminate startup latency and memory overhead.
- Enhanced CrashHandler: intercepts uncaught exceptions immediately at startup and writes diagnostics directly to Android/data/com.karlitodev.novelize/logs/crash.txt and crah.txt for easy inspection.
- Advanced code shrinking and R8 obfuscation protection with dedicated keep rules for Navigation 3 (NavKey) serialization to protect intellectual property while ensuring runtime stability.
- Configured maxSdk to 37 and secured activity lifecycle initialization.

## [1.4] - 2026-10-21

### New Features
- In-app Release Notes screen integrated directly into Settings, showing complete version history without emojis.
- Automated deletion of stale GitHub Actions builds: retains only the 10 most recent runs, pruning older records via the GitHub API.

### Bug Fixes and Optimizations
- Resolved R8 compilation failure caused by missing optional class com.gemalto.jp2.JP2Decoder in PDFBox-Android. Fixed using ProGuard -dontwarn rules.
- Added -keep rules for ONNX Runtime and ML Kit to prevent accidental removal by R8 minification.
- Corrected hardcoded version string in library screen to synchronize changelog notification dialog on launch.
- Defaulted release notes display to English.

## [1.3] - 2026-10-21

### New Features
- Multilingual Regex gender detection (male and female characters) for French (FR), Japanese (JP), Korean (KR), and English (EN).
- Dynamic assignment of 4 distinct character voice profiles (2 male voices: MALE_1 and MALE_2; 2 female voices: FEMALE_1 and FEMALE_2) in addition to the narrator, each with customized pitch and cadence modulation.
- Extended local neural voice engine (Piper) supporting secondary male and female voice models in French and English.

### Bug Fixes and Optimizations
- Resolved PDFBox-Android library imports, fixing continuous integration build issues.

## [1.2] - 2026-10-21

### New Features
- Full multimedia controls in status bar, notification shade, and lock screen via MediaSessionCompat and NotificationCompat.MediaStyle (play, pause, previous sentence, next sentence).
- Native support for PDF documents with text extraction and automatic cover rendering via PdfRenderer and PDFBox-Android.
- Native support for comic and manga CBZ archives with embedded cover extraction.
- Native support for Markdown text files (.md, .markdown) with chapter heading detection.
- Enhanced EPUB parsing with container.xml manifest inspection and .opf spine reading order resolution.
- Automatic extraction and display of first-page or embedded covers for all supported formats.
- Manual custom cover replacement for any library book directly from device image picker.
- Direct book removal option from the library bookshelf.
- Integrated CrashHandler diagnostic reporting saving crash.txt with one-tap WhatsApp sharing to developers.
- Intelligent SFX synchronization from GitHub with integrity checks to prevent redundant downloads.
- Localized welcoming toasts and notification banners matching user language.
- Smart resume reading: flashing highlight on the last spoken phrase with screen focus dimming.

### Bug Fixes and Optimizations
- Removed 32 synthetic python-generated sound effects from repository to improve audio fidelity.
- Preserved 18 studio-grade physical sound effects (combat, magic, interactions).
- Replaced synthetic voice engine with Android system TextToSpeech modulated by character role.
- Redesigned SFX detection keywords to eliminate false positives on common words.
- Added cooldown interval preventing repeated sound effects within 8 paragraphs.
- Published direct signed APK on GitHub Releases without ZIP wrapping for seamless self-updates.
- Corrected splash entrance animation scaling on launch.

## [1.1] - 2026-10-21

### Additions
- Setup Wizard executed once on initial application launch.
- Silent background update detection via GitHub Releases API.
- Improved background narration playback service stability.
