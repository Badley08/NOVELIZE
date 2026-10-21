# Bilan Technique et Feuille de Route d'Amelioration - NoveLize

> Date d'analyse : 21 Octobre 2026
> Projet : NoveLize (Android - Jetpack Compose, Kotlin, Audio Drama)
> Objectif : Etablir un audit transparent et exhaustif entre les specifications et le code source reel, identifier les faiblesses techniques et proposer un plan d'action d'amelioration structure.

---

## Sommaire
1. [Synthese Globale & Indice de Realite](#1-synthese-globale--indice-de-realite)
2. [Fonctionnalites Non Faites ou Simulees](#2-fonctionnalites-non-faites-ou-simulees)
3. [Priorite Audio : Mixage SFX et Fond Sonore](#3-priorite-audio--mixage-sfx-et-fond-sonore)
4. [Axes d'Amelioration Technique](#4-axes-damelioration-technique)
5. [Matrice des Priorites et Plan d'Action](#5-matrice-des-priorites-et-plan-daction)

---

## 1. Synthese Globale & Indice de Realite

| Domaine Fonctionnel | Annonce dans la documentation | Realite dans le code | Niveau d'implementation |
| :--- | :--- | :--- | :--- |
| Moteur Vocal Neural IA | Modeles neuronaux VITS/Piper ONNX on-device, 5 roles | Simple Android TTS systeme natif avec pitch/rate modifies | Partiel (25%) |
| Effets Sonores (50 SFX) | Catalogue de 50 SFX Pixabay, detection automatique | 18 fichiers presents dans SFX, 2 dans res/raw, 32 absents | Incomplet (36%) |
| Mixage SFX vs Narration | Ducking et equilibre | Concurrence directe, superposition non feutree | A corriger en priorite |
| Traducteur Hors-Ligne | Dictionnaire bilingue bidirectionnel EN / FR | Map Kotlin de 70 mots | Fictif / Ebauche (10%) |
| Import Documents (EPUB/PDF/DOCX) | Parseur universel robuste | PDF regex ISO-8859-1 souvent defectueux, EPUB sans spine | Fragile / Partiel (35%) |
| Quiz Comprehension NLP | Analyse semantique dynamique | Heuristique basique, distracteurs en anglais | Partiel (40%) |
| Persistance des Donnees | Persistance complete livres et signets | Fichiers JSON monolithiques reecrits en bloc | Rudimentaire (50%) |
| Auto-Updater GitHub | Mise a jour APK via GitHub Releases | Implemente et fonctionnel | Fonctionnel (85%) |
| Interface Jetpack Compose | Themes, menu contextuel, animations | UI reactive et moderne | Fonctionnel (80%) |

---

## 2. Ce qui n'est PAS VRAIMENT FAIT (Simulations, Mocks & Écarts)

### 2.1. Le Moteur Vocal IA ("Piper / VITS Neural Models")
* **Ce qui est promis :** `FEATURES.md` et `AiVoiceEngine.kt` affirment que l'application exécute de vrais modèles neuronaux d'IA vocale (Piper/VITS au format `.onnx`).
* **La réalité du code :**
  - Aucune dépendance ONNX Runtime (`com.microsoft.onnxruntime:onnxruntime-android`) n'est présente dans `app/build.gradle.kts`.
  - La classe `AiVoiceEngine.kt` ne contient aucune méthode d'inférence. Elle ne fait que déclarer une liste de noms de fichiers (`.onnx`) et une heuristique de langue.
  - Le lecteur utilise en réalité le composant système standard `android.speech.tts.TextToSpeech`. Les rôles (Héros, Femme 1, Narrateur, etc.) se résument à changer la valeur numérique du `pitch` (0.78f à 1.30f) et du `speechRate` sur la voix TTS par défaut du smartphone Android.

### 2.2. Le Service de Traduction Hors-Ligne
* **Ce qui est promis :** "Comprehensive offline translation service with extensive bidirectional English ⇄ French vocabulary".
* **La réalité du code :**
  - Le fichier `DefaultTranslationService.kt` contient une simple `Map<String, String>` statique d'environ **70 mots** (ex: *blade, sword, castle, dragon*).
  - Dès qu'un lecteur clique sur un mot qui n'est pas dans cette minuscule liste (par exemple *determination*, *scoundrel*, *pathway* ou la conjugaison d'un verbe régulier), le service renvoie : `« mot » (Vocabulaire hors dictionnaire local)`.
  - Ce n'est ni un vrai dictionnaire embarqué (qui nécessiterait au minimum 30 000 à 60 000 mots sous SQLite), ni un modèle de traduction automatique on-device (comme Google ML Kit Translate Offline).

### 2.3. L'Analyseur de Documents Universel (PDF, EPUB, DOCX)
* **PDF (`parsePdfStream`) :**
  - L'analyseur lit le flux brut d'octets en encodage `ISO_8859_1` et cherche des balises de flux `BT ... ET` avec une regex naïve `\(([^)]*)\)\s*(?:Tj|')`.
  - **Problème majeur :** 95% des PDFs modernes utilisent la compression zlib/FlateDecode et des encodages de polices (ToUnicode CMap). Le parseur ne trouve aucun texte et tombe immédiatement dans sa condition de repli :
    ```kotlin
    val fullContent = if (extracted.length > 30) extracted else "PDF Document: $title\n\nChapter 1\nImported PDF contents ready for audio drama narration."
    ```
    L'utilisateur se retrouve avec un texte factice au lieu du contenu de son livre !
* **EPUB (`parseEpubStream`) :**
  - L'extraction trie les fichiers HTML par ordre alphabétique (`xhtmlFiles.entries.sortedBy { it.key }`).
  - **Problème :** Dans un EPUB standard, l'ordre des chapitres est dicté par le `<spine>` du fichier `content.opf`. Un tri alphabétique brise la chronologie du livre (ex: `chapter10.xhtml` arrive avant `chapter2.xhtml`).
* **Attribution des Locuteurs de Dialogue :**
  - Le code alterne simplement un booléen :
    ```kotlin
    val speaker = if (isDialogue) {
        speakerToggle = !speakerToggle
        if (speakerToggle) SpeakerRole.HERO else SpeakerRole.FEMALE_1
    }
    ```
    Il n'y a aucune analyse NLP réelle pour savoir quel personnage parle ; chaque réplique passe alternativement du héros à l'héroïne, même s'il s'agit d'un dialogue entre deux hommes ou d'un groupe.

### 2.4. Le Générateur de Quiz de Compréhension
* Les distracteurs (fausses réponses) pour les questions d'événements sont rédigés en dur en anglais dans le code :
  ```kotlin
  val distractor1 = "The characters decided to rest and do nothing for the rest of the day."
  val distractor2 = "$charName decided to completely abandon the journey and return home."
  ```
  Sur un roman en français, l'utilisateur voit une vraie phrase en français et 3 fausses réponses écrites en anglais !

### 2.5. Le Catalogue des 50 SFX
* `SFX.md` liste 50 bruitages requis.
* Seuls 2 fichiers MP3 se trouvent dans `app/src/main/res/raw/` (`sword_clash.mp3`, `sword_slash.mp3`).
* 13 fichiers supplémentaires sont dans le dossier racine `SFX/`, mais aucun script ou code ne les copie dans les ressources de l'application ou dans le dossier externe de l'appareil lors de l'installation. À l'exécution, 48 effets sur 50 sont inaccessibles par défaut.

---

## 3. Priorité Spéciale : Audio & Mixage SFX / Voix (Note dans `txt.txt`)

> **Rappel de la consigne utilisateur (`txt.txt`) :**  
> *"continue fait en sorte que le son des sfx ne se melange pas avec narration et autres de le mettre comme un background sound"*

### Diagnostic du problème actuel :
1. **Concurrence Audio Directe :**
   - Le TTS Android (`TextToSpeech`) et les bruitages courts (`SoundPool`) jouent sur le même flux audio standard (`USAGE_MEDIA`).
   - Lorsqu'un mot-clé déclenche un SFX (comme une explosion ou un coup d'épée), le bruitage se superpose brutalement à la voix du TTS au même volume ou la rend inintelligible.
2. **Gestion du Ducking trop superficielle :**
   - La méthode `applyDucking()` dans `SfxAudioPlayer` réduit théoriquement le volume du MediaPlayer d'ambiance, mais le SoundPool joue ses one-shots sans canal audio dédié ni temporisation.
3. **Absence d'un canal "Background Soundscape" unifié :**
   - Pour que les SFX ne s'entrechoquent pas avec la voix de lecture :
     - Les bruits d'ambiance (pluie, taverne, vent, feu) doivent être sur un canal de fond continu à volume feutré (20-30%).
     - Les bruitages d'action (SFX ponctuels) doivent être insérés soit **dans les micro-pauses entre deux phrases**, soit joués avec une atténuation immédiate de la voix TTS (ducking d'AudioTrack ou pause de 300ms du TTS), soit spatialisés comme son d'arrière-plan avec un filtre ou un volume bas pour ne jamais dominer la voix narratrice.

---

## 4. Ce qui est À AMÉLIORER d'Urgence (Architecture & Dette Technique)

### 4.1. Base de Données & Stockage (Remplacer le JSON par Room/SQLite)
* **Problème :** Actuellement, `DefaultNovelizeRepository` enregistre tous les chapitres d'un livre dans un unique fichier texte JSON (`chapters_{bookId}.json`).
  - Pour un livre de 100 000 mots, cela crée un fichier de plusieurs mégaoctets parsé en mémoire à chaque ouverture.
  - Risque majeur de `OutOfMemoryError`, saccades de l'interface et corruption en cas de fermeture brutale de l'application.
* **Solution :** Implémenter **Room Database** avec des entités :
  - `BookEntity`
  - `ChapterEntity`
  - `ParagraphEntity`
  - `ReadingProgressEntity`
  - `HighlightEntity`

### 4.2. Intégration Réelle de Modèles Vocaux Neuronaux
* Pour respecter la promesse de `FEATURES.md` :
  - Soit intégrer **Sherpa-ONNX** (moteur open-source léger pour Android basé sur ONNX Runtime, supportant Piper, VITS et Whisper hors-ligne).
  - Soit clarifier honnêtement dans l'application qu'il s'agit d'un **TTS Système Amélioré avec Profils Stylisés** tant que les modèles neuronaux (qui pèsent chacun entre 50 et 100 Mo) ne sont pas téléchargés via un gestionnaire d'assets optionnel.

### 4.3. Remplacement du Parseur PDF
* Intégrer la bibliothèque officielle Android `PdfRenderer` ou une bibliothèque de parsing texte comme `PdfBox-Android` pour extraire proprement le texte des PDFs réels au lieu d'une regex binaire inopérante.

### 4.4. Enrichissement du Dictionnaire / Traducteur
* Intégrer un vrai dictionnaire SQLite bilingue hors-ligne d'au moins 30 000 lemmes (format Stardict ou base de données SQLite packagée dans `assets/`), ou le module hors-ligne **ML Kit Translate** de Google.

### 4.5. Pack des SFX embarqués
* Déplacer les 15 MP3 présents dans le dossier `SFX/` vers `app/src/main/res/raw/` et ajouter un mécanisme d'extraction automatique depuis `assets/` vers `context.getExternalFilesDir("sfx")` au premier lancement pour que l'utilisateur dispose immédiatement des effets sonores.

### 4.6. Problème Détecté dans la Suite de Tests Unitaires (9 tests en échec)
* Lors de l'exécution de `./gradlew testDebugUnitTest`, **9 tests sur 95 échouent** dans `MainScreenViewModelTest` :
  - `searchQuery_filtersBooksByTitle` (ClassCastException)
  - `searchQuery_filtersBooksByAuthor` (ClassCastException)
  - `searchQuery_emptyReturnsAllBooks` (ClassCastException)
  - `sortMode_defaultIsLastRead` (ClassCastException)
  - `sortMode_titleAscSortsByTitleAlphabetically` (ClassCastException)
  - `sortMode_progressSortsByReadingPercentDescending` (ClassCastException)
  - `totalBooks_reflectsUnfilteredCount` (ClassCastException)
  - `uiState_successAfterBooksEmitted` (AssertionFailedError)
  - `uiState_emptyBooksShowsEmptyList` (AssertionFailedError)
* **Cause racine :** Le `StateFlow` `uiState` dans `MainScreenViewModel` utilise `SharingStarted.WhileSubscribed(5_000)`. Dans les tests unitaires sans collecteur actif (`backgroundScope.launch { viewModel.uiState.collect {} }`), le flux combiné n'est jamais démarré et `uiState.value` reste bloqué à sa valeur initiale `MainScreenUiState.Loading`, provoquant un crash par cast vers `MainScreenUiState.Success`.
* **Correction recommandée :** Rendre le `SharingStarted` configurable ou lancer une collecte active dans les tests via `backgroundScope`.

---

## 5. Matrice des Priorités & Plan d'Action Recommandé

```
[URGENCE ÉLEVÉE]
  │
  ├─ 1. Fix Audio & SFX Background : Séparer le mixage SFX / Narration pour que les SFX
  │     agissent en fond sonore sans étouffer ou parasiter la voix (Consigne txt.txt).
  │
  ├─ 2. Rapatrier les 15 fichiers audio du dossier SFX vers l'application (res/raw ou assets).
  │
  ├─ 3. Internationaliser les distracteurs du Quiz (détecter FR/EN pour ne pas mélanger les langues).
  │
[URGENCE MOYENNE]
  │
  ├─ 4. Migration de la persistance JSON vers Android Room Database (SQLite).
  │
  ├─ 5. Révision du parser EPUB (respecter l'ordre des chapitres du Manifest/Spine OPF).
  │
  ├─ 6. Remplacement du parser PDF regex par une extraction de texte réelle (ex: PdfBox-Android).
  │
[URGENCE LONG TERME]
  │
  ├─ 7. Vraie intégration de modèles Piper/VITS (Sherpa-onnx) ou clarification de la documentation.
  │
  └─ 8. Enrichissement du traducteur avec un lexique complet de plus de 20 000 mots.
```

---
*Ce document sert de référence technique pour les prochaines étapes de développement du projet NoveLize.*
