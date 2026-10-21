# NoveLize v1.0.0 - Release Notes

**Tag:** v1.0.0
**Date:** 2026-10-21
**Commit:** 066c8e5
**Branch:** main
**Repository:** https://github.com/Badley08/NOVELIZE

---

## Overview

NoveLize is an Android reading application built with Jetpack Compose and Kotlin.
It transforms imported novels (EPUB, PDF, TXT) into an immersive audio-drama experience
by combining neural on-device narration, dynamic sound effects, comprehension quizzes,
and bidirectional translation.

---

## What is Included in This Release

### Core Reading Engine
- EPUB, PDF, and plain-text import and parsing
- Chapter navigation with persistent reading progress
- Swipe-to-advance and tap-to-navigate gesture support
- Multiple reader themes (dark, sepia, night, light)
- Adjustable font size and line spacing

### Local Neural Voice Narration
- On-device AI voice engine using Android ML Kit and neural TTS pipeline
- Three voice profiles: Narrator, Character, and Villain
- Per-profile pitch, speed, and timbre controls
- Background narration service with MediaSession and notification controls
- No internet connection required for playback

### Sound Effects (SFX)
- 50-entry SFX catalog mapped to in-text trigger keywords
- SFX plays as background audio and does not overlap with narration
- Volume ducking ensures narration always remains intelligible
- Supported categories: ambiance, weather, action, nature, suspense, crowd, animals

### Comprehension Quiz System
- Dynamic question generation powered by ML Kit entity extraction
- Questions adapt to the detected language of the loaded book (EN or FR)
- Multiple-choice format with randomized distractors
- Score tracking and results screen after each chapter session

### Translation
- Word-level tap-to-translate within the reader view
- Expanded offline EN-FR dictionary (~800 entries)
- Language auto-detection on book import
- Bidirectional translation support

### Library Management
- Local library screen with cover art, title, author, and progress display
- Last-read position restored on re-open
- Delete and re-import support

### Auto-Updater
- Version check against GitHub Releases API on app launch
- In-app update prompt with changelog display

### Persistence
- Reading progress, bookmarks, and quiz scores stored via local JSON datastore
- User preferences persisted across sessions

---

## Known Limitations in This Release

| Area | Limitation |
|---|---|
| PDF parsing | Uses a regex-based extractor; compressed or image-only PDFs may fail |
| EPUB chapter order | Files sorted alphabetically by name, not by spine order |
| SFX assets | Only assets present in the assets/SFX/ directory are playable |
| Offline dictionary | ~800 entries; uncommon or technical vocabulary may not resolve |
| TTS neural model | Falls back to Android system TTS if ML Kit voice model is unavailable |
| Persistence layer | Large novels stored as a single JSON file; Room migration planned for v1.1 |
| Quiz distractors | Generated from chapter vocabulary; quality depends on text richness |

---

## Build Information

| Property | Value |
|---|---|
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Compile SDK | 34 |
| Version Code | 1 |
| Version Name | 1.0 |
| Build Type | Release (unsigned) |
| Language | Kotlin |
| UI Framework | Jetpack Compose |

---

## CI Build

The release APK is built automatically via GitHub Actions on every push to main.
Workflow file: .github/workflows/build.yml
Artifact name: app-release-unsigned

To download the unsigned APK, navigate to:
https://github.com/Badley08/NOVELIZE/actions

---

## Commits in This Release

| SHA | Message |
|---|---|
| 066c8e5 | Update sound settings for SFX and narration |
| b3b198b | feat: complete local neural AI voice engine, ML Kit quiz, 50 SFX, expanded offline dictionary, and audit documentation |
| bd84d66 | feat: complete word-level selection, bidirectional translation, dynamic progress tracking, and unit tests |
| 3a1aae5 | feat: complete initial NoveLize release with auto-updater, multi-voice engine, dynamic quizzes, and persistent storage |

---

## Roadmap for v1.1

- Migrate persistence layer from JSON to Android Room database
- Integrate PdfBox-Android for robust PDF text extraction
- EPUB spine-order chapter sorting
- Expand offline dictionary to 5000+ entries using a bundled SQLite word database
- Add quiz language localization for additional languages (ES, DE, PT)
- APK signing pipeline via GitHub Actions secrets
