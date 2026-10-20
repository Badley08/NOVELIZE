# NoveLize Features

> NoveLize is in the design phase. Everything below describes planned behavior for the first release (v1), not shipped features. For the project overview, stack, and license, see the [README](README.md).

---

## Resume & Navigation

* **Automatic resume:** The app stores the exact chapter, paragraph, and word where reading or narration stopped. When the book is reopened, the narration restarts from that word.
* **Narration picks up where it stopped:** After a pause, an interruption (phone call, headset disconnected), or closing the app, playback resumes from the last spoken word, not from the start of the sentence or chapter.
* **Back to where I stopped:** A button returns to the last playback position after the reader has scrolled elsewhere or browsed other chapters.
* **Manual stop marker:** The reader can mark any word as the place where they stopped (see the long-press menu below). The marker is kept alongside the automatic position.

---

## Long-Press Menu

Long-pressing a word (or a selected passage) opens a menu with these actions:

* **Mark as where I stopped:** Sets the stop marker on that word.
* **Translate:** Shows the offline translation of the word.
* **Highlight:** Marks the text with a color.
* **Underline:** Underlines the text.

---

## Local Multi-Voice Audio Drama

* **Automatic dialogue speaker assignment:** Detects dialogue segments and maps a distinct voice profile to each of up to 5 speakers (for example male, female, teenager, child, narrator).
* **On-device text-to-speech:** Voice synthesis runs locally. No audio or text is sent to a cloud service.
* **Engine:** Piper voices running through Sherpa-ONNX, chosen for its range of distinct French and English voices and its low resource use on modest phones (4 GB of RAM).
* **Optional high-quality pack:** Kokoro voices, downloadable on demand for the best English quality, on devices powerful enough to run them.
* **Fallback:** If a device cannot run the neural voices, the app falls back to the Android system voice.
* **Narration language:** English or French, following the language of the imported file. An English file is narrated in English, a French file in French.

---

## Contextual Sound Design (SFX)

* **Real-time soundscapes:** Triggers ambient audio loops and targeted sound effects aligned with action beats and mood changes in the text.
* **Source of the sounds:** To be decided (embedded, downloadable, or both).

---

## Synchronized Visual Progress

* **Karaoke highlighting:** Highlights the active sentence in real time during playback.

---

## Offline Translation

* **On-device ML translation:** Uses Google ML Kit translation models, so translation needs no API subscription and no internet connection once the models are downloaded.
* **Word translation:** Long-press a word and choose Translate to see its translation without leaving the page.

---

## Comprehension Validation

* **Lightweight NLP quizzes:** Analyzes character interactions and key plot points on the device at the end of a chapter to offer a quick comprehension check.

---

## Import & Reading

* **Supported formats:** EPUB, TXT, PDF, and DOCX. Only DRM-free, user-provided files are supported.
* **EPUB illustrations:** Images embedded in EPUB files are displayed inline in the reader.
* **Themes and fonts:** Light, sepia, and night themes, plus font choice and size.
* **Dynamic book themes:** The reader's colors adapt to the dominant color of each book's cover, so every book gets its own look. This can be turned off in favor of a fixed theme.

---

## Playback

* **Background playback:** Narration continues with the screen locked, with lock screen and Bluetooth headset controls.
* **Sleep timer:** Stops playback automatically after a chosen delay.

---

## Library

* **Bookshelf:** Imported books shown with their covers and reading progress.

---

## First-Run Setup

On first launch, a setup wizard lets the user configure the app before reading:

* **Appearance:** Base theme, font, font size, and whether book themes follow the cover color.
* **Resources:** Which voice models and translation languages to download.
* **Optimal-use notice:** A short screen explains how to get the best experience: use NoveLize on its own, avoid resource-heavy apps in the background, and for long sessions turn off the screen and use headphones. The screen shows a 3D headphones illustration.

---

## Standalone Onboarding

* **Built-in original content:** Locally authored demo stories showcase voices, translation, and audio effects right after installation.
* **On-demand resource download:** The core app stays lightweight. Voice models and other assets are fetched through a first-run setup wizard.
