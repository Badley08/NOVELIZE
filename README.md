<div align="center">

![NoveLize Banner](https://img.shields.io/badge/NoveLize-Interactive_Light_Novel_Reader-6C5CE7?style=for-the-badge&logo=android&logoColor=white)

[![Status](https://img.shields.io/badge/Status-In_Design-orange?style=flat-square)](#project-status)
[![Android Version](https://img.shields.io/badge/Target-Android_10%2B_%28API_29%2B%29-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Privacy](https://img.shields.io/badge/Design-100%25_Offline_%2F_Privacy_First-000000?style=flat-square&logo=github&logoColor=white)](#technical-stack)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue?style=flat-square)](LICENSE)

**Immersive & Cinematic Light Novel Reader**

*Bring your novels to life with on-device multi-voice narration, dynamic soundscapes, and offline translation.*

</div>

---

## Project Status

> **NoveLize is currently in the design phase. There is no usable release yet.**
> This document describes the planned application. Everything below is a target, not a shipped feature, and details may change as development starts.

---

## Overview

**NoveLize** is a planned Android application that aims to redefine the light novel reading experience. By combining offline artificial intelligence with physics-based UI animations, it will turn static text into an immersive audio performance, without relying on a central backend and without hosting any copyrighted content.

All features are planned for the first release (v1).

---

## Planned Features

| Feature | Description |
| :--- | :--- |
| **Offline multi-voice TTS** | Local text-to-speech with up to 5 distinct voices |
| **Contextual sound design** | Ambient loops and sound effects that follow the mood of the text |
| **Smart resume** | Narration restarts at the exact word where it stopped, with a button to jump back to it |
| **Long-press menu** | Long-press a word to mark where you stopped, translate, highlight, or underline |
| **Synchronized highlighting** | Active sentence highlighted during playback |
| **Offline translation** | On-device translation of words and passages |
| **Comprehension quizzes** | Short on-device quizzes at the end of each chapter |
| **Background playback** | Listen with the screen locked, with a sleep timer |
| **Reading comfort** | Light, sepia, and night themes, fonts, and dynamic themes that follow each book's cover color |
| **First-run setup** | Guided configuration of theme, voices, and languages at launch |
| **Library** | Bookshelf with covers and reading progress |
| **Import formats** | EPUB (with illustrations), TXT, PDF, and DOCX |

Full details of every feature are in [FEATURES.md](FEATURES.md).

---

## Technical Stack

| Layer | Component | Specification |
| :--- | :--- | :--- |
| **Language** | Kotlin | Coroutines and Flow for asynchronous work |
| **UI** | Jetpack Compose | Material 3 with physics-based motion |
| **Minimum SDK** | Android 10 | API level 29 (target API 34) |
| **Local database** | Room | Reading progress, settings, and speaker mapping |
| **Translation** | ML Kit (on-device) | Offline translation models |
| **Audio pipeline** | Jetpack Media3 / ExoPlayer | Multi-track playback for ambient audio and TTS output |
| **TTS engine** | Sherpa-ONNX with Piper voices | Kokoro as an optional download; Android system TTS as fallback |
| **SFX source** | To be decided | Embedded, downloadable, or both |

---

## System Requirements (Planned)

Local inference for speech synthesis is resource-intensive:

* **Minimum OS:** Android 10 (API 29)
* **Recommended RAM:** 4 GB or more
* **Storage:** about 150 MB for the core app, plus extra space for optional voice modules

> **Performance note:** for the best experience, use NoveLize on its own, avoid resource-heavy apps in the background, and for long sessions turn off the screen and use headphones. The app shows this notice at first launch.

---

## Neutral Client Model

NoveLize follows a strict **Neutral Client** model:

1. **Bare application engine:** The install package contains no web scraping tools and no link to copyrighted content repositories.
2. **First-run wizard:** On first launch, an optional asset manager lets users download the offline voice profiles and translation modules they need.
3. **Local file support:** The app works only on user-provided, DRM-free local documents.

---

## Implementation Progress & Roadmap

- [x] Vector branding, launcher adaptive icons, and physics-based spring animations
- [x] Scaffolding: Jetpack Compose, Material 3, Navigation, Coroutines & StateFlow
- [x] Universal document import: offline DRM-free EPUB, TXT, PDF, and DOCX parsers
- [x] Dialogue segmentation & automated multi-voice profile allocation (Lead, Female, Male, Supporting, Narrator)
- [x] Smart resume: exact word, sentence, and chapter position stored in persistent storage with floating resume button
- [x] Word-level long-press contextual actions: offline translation, stop bookmarking, color highlights, underlines
- [x] Contextual sound design engine: real-time SFX triggers and acoustic playback
- [x] Synchronized karaoke sentence progression with visual progress tracking
- [x] Sleep timer and uninterrupted background playback support
- [x] Reading comfort: Light, Sepia, Night, and Dynamic Cover themes with adjustable font size
- [x] Bidirectional offline translation service (English ⇄ French)
- [x] Dynamic on-device NLP comprehension quiz generation for any user-imported novel
- [x] First-run onboarding wizard with optimal-use hardware guidelines and built-in demo story
- [x] Automated self-updater directly from GitHub Releases (detection, progress download, FileProvider APK installer)
- [ ] High-fidelity on-demand neural voice pack downloads (Sherpa-ONNX / Kokoro)

---

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0). See the [LICENSE](LICENSE) file for details.

The GPL-3.0 choice comes from the speech stack: Piper and the espeak-ng phonemizer it relies on are GPL-3.0 software. Individual voice models and the optional Kokoro voices carry their own licenses, which will be listed in the app.
