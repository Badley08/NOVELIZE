# NoveLize Features & Architecture

> NoveLize is an immersive, local-first light novel reader and audio drama engine for Android.
> Below describes the implemented feature architecture and specifications for the application.

---

## Resume & Navigation

* **Automatic resume:** The app stores the exact chapter, paragraph, sentence, and word where reading or narration stopped. When the book is reopened, narration and visual progress restart from that exact position.
* **Narration picks up where it stopped:** After pauses or app closure, playback resumes from the last position without losing place.
* **Back to where I stopped:** A floating resume action button appears whenever the reader scrolls away from the active narration paragraph, allowing instant return to the current reading position.
* **Manual stop marker:** Readers can long-press any word or paragraph to mark a custom stop point. The marker persists alongside automatic position tracking.

---

## Long-Press Menu

Long-pressing any word in a passage opens a contextual action dialog:

* **Mark as where I stopped:** Sets a manual stop bookmark on that word.
* **Translate (Offline):** Instant on-device bilingual dictionary lookup (English ⇄ French) tailored for light novel prose.
* **Highlight:** Applies text highlight color styling.
* **Underline:** Underlines selected passage.

---

## Local Multi-Voice Audio Drama (Neural AI Models)

* **Real Neural AI voice models:** Exclusively supports French and English literary texts using dedicated neural voice models (Piper/VITS neural architectures) mapped across 5 speaker roles:
  * Narrator (neutral pitch and balanced cadence)
  * Lead Character (slightly lower pitch, dynamic tempo)
  * Female Voice 1 (higher pitch, clear articulation)
  * Male Voice 1 (deep resonance, steady cadence)
  * Supporting Characters (varied timbre)
* **Strict language restriction:** Text in languages other than French or English is automatically refused and flagged to prevent degraded synthesis.
* **Continuous background playback:** Powered by an Android `ForegroundService` with media notifications and lock-screen controls.
* **On-device execution:** 100% private, zero telemetry, and zero cloud dependencies.

---

## Contextual Sound Design (50 SFX Checklist)

* **50-item sound effect catalog:** Covers combat, magic, ambient loops, places, mood/tension, and travel.
* **Local Pixabay sound storage:** Automatically detects and loads user-downloaded Pixabay MP3/OGG sound files placed in the app directory (`/Android/data/com.karlitodev.novelize/files/sfx/`).
* **SoundPool & ambient MediaPlayer integration:** One-shot effects play via low-latency `SoundPool`, while ambient soundscapes loop seamlessly via `MediaPlayer`.
* **Volume balancing:** Separate volume sliders for SFX and ambient loops in settings.

---

## Synchronized Visual Progress

* **Karaoke highlighting:** The active sentence is highlighted in real time during playback, advancing automatically.
* **Auto-scroll:** The reader view automatically scrolls to keep active dialogue and text centered on screen.

---

## Offline Translation

* **Bidirectional bilingual lexicon:** Includes comprehensive English ⇄ French vocabulary spanning fantasy, battle terminology, setting descriptions, and character dialogues.
* **Language auto-detection:** Detects French and English terms and provides immediate translations without leaving the page.

---

## Dynamic Comprehension Validation (NLP)

* **Zero hardcoded questions:** Analyzes the actual text of whatever novel or document is imported.
* **Quote speaker verification:** Detects dialogue quotes and queries which character spoke that specific line in the chapter.
* **Plot event verification:** Identifies action beats and creates multiple-choice verification questions.
* **Opening and concluding analysis:** Tests recall of chapter beginnings and climaxes.

---

## Universal Document Import

* **Supported formats:** EPUB, TXT, PDF, and DOCX (DRM-free).
* **EPUB extractor:** Unzips archive, parses `content.opf` metadata, extracts XHTML chapters in reading order, and strips formatting.
* **DOCX extractor:** Reads `word/document.xml`, parses paragraph elements, and structures into chapters.
* **PDF parser:** Extracts text stream objects (`BT ... ET`) and renders clean reading passages.

---

## Automated Self-Updates (GitHub Releases)

* **Direct GitHub Releases API integration:** Queries latest release tags and changelogs from `Badley08/NOVELIZE`.
* **In-app downloader:** Downloads `.apk` release assets with real-time download progress.
* **Package installer:** Triggers Android system package installation via `FileProvider` without requiring Google Play Store services.

---

## Library & Bookshelf

* **Visual bookshelf:** Displays cover representations, chapter counts, and real reading progress percentages.
* **Persistent private storage:** Novel metadata, parsed chapters, reading progress, and settings persist across app reboots in internal application storage.

---

## First-Run Setup & Splash Screen

* **Physics launch screen:** Spring-driven logo animations with bouncy overshoot, orbital particles, and pulsing sound waves.
* **Permission requests on launch:** Storage access (`READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`) and battery optimization exemption (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) for uninterrupted background reading.
* **Setup wizard:** Configures initial theme, typography sizing, and presents optimal-use hardware guidelines.
