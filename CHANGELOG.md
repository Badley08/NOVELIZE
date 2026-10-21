# Journal des Modifications - NoveLize

Toutes les modifications notables de ce projet sont documentees dans ce fichier.
Ce journal ne contient aucun emoji conformement aux exigences de versionnage.

## [1.4] - 2026-10-21

### Nouveautes
- Ecran "Notes de Version" integre directement dans l'application, accessible depuis les Parametres, affichant l'historique complet de toutes les versions sans aucun emoji.
- Suppression automatique des anciens builds GitHub Actions : seuls les 10 runs les plus recents sont conserves, les autres sont effaces via l'API GitHub.

### Corrections et Optimisations
- Correction de l'erreur de compilation R8 causee par la classe manquante com.gemalto.jp2.JP2Decoder (filtre JPX optionnel de PDFBox-Android non present sur Android). Resolution via regles ProGuard -dontwarn.
- Ajout de regles -keep pour ONNX Runtime et ML Kit afin d'eviter toute suppression accidentelle par le minificateur R8.
- Correction de la version codee en dur dans la bibliotheque (passage de 1.2 a 1.3, puis 1.4) pour synchroniser correctement le badge de nouveaute au demarrage.
- Notes de version affichees en anglais par defaut.

## [1.3] - 2026-10-21

### Nouveautes
- Detection multilingue par expressions regulieres (Regex) du genre des personnages (masculin et feminin) pour les langues francaise (FR), japonaise (JP), coreenne (KR) et anglaise (EN).
- Attribution dynamique de 4 voix de personnages distinctes (2 voix masculines : MALE_1 et MALE_2, 2 voix feminines : FEMALE_1 et FEMALE_2) en plus du narrateur, avec reglages acoustiques differencies de hauteur tonale (pitch) et de cadence.
- Extension du moteur vocal neural local et IA (Piper) pour supporter les modeles de voix secondaires masculins et feminins en francais et en anglais.

### Corrections et Optimisations
- Correction de l'importation de la bibliotheque PDFBox-Android (com.tom_roush.pdfbox) resolvant l'erreur de compilation en integration continue.

## [1.2] - 2026-10-21

### Nouveautes
- Controles multimedia complets dans la barre d'etat, la zone de notification et l'ecran de verrouillage Android via MediaSessionCompat et NotificationCompat.MediaStyle (lecture, pause, phrase precedente, phrase suivante).
- Support natif des documents PDF avec extraction de texte et generation automatique de couverture via PdfRenderer et PDFBox-Android.
- Support natif des bandes dessinees et mangas au format archive CBZ avec extraction de couverture.
- Support natif des fichiers texte Markdown (.md, .markdown) avec hierarchisation des titres de chapitres.
- Support ameliore des fichiers EPUB avec analyse structuree du fichier de manifeste container.xml et de l'ordre de lecture du spine (.opf).
- Extraction et affichage automatique de la premiere page ou de la couverture integree pour tous les formats compatibles.
- Possibilite pour l'utilisateur de definir et remplacer manuellement la couverture de n'importe quel livre a partir de la galerie d'images.
- Option de suppression directe des livres importes de la bibliotheque.
- Module de gestion des incidents et crashs (CrashHandler) generant un rapport technique detaille crash.txt et permettant son envoi direct par WhatsApp aux developpeurs.
- Synchronisation intelligente des effets sonores (SFX) depuis le depot GitHub avec verification d'integrite pour eviter tout re-telechargement de fichiers deja presents.
- Messages d'information et d'accueil personnalises selon la langue utilisateur ("Yo ! Y a de nouveaux sons...", "Yo ! Une nouvelle mise a jour est disponible...").
- Reprise de lecture intelligente : mise en surbrillance clignotante du texte ou la lecture s'est arretee et assombrissement temporaire de l'interface pour guider l'attention.

### Corrections et Optimisations
- Suppression des 32 effets sonores synthetiques generes par python dans les ressources locales et du depot GitHub afin d'ameliorer la qualite audio.
- Conservation et integration des 18 effets sonores reels de qualite studio (combat, magie, interactions).
- Remplacement du synthetiseur vocal synthetique par le moteur TextToSpeech systeme natif d'Android, personnalise par role vocal (hauteur de voix et cadence distinctes pour le narrateur, le heros, les personnages feminins et masculins).
- Refonte des regles de detection des effets sonores pour empecher les faux positifs sur les mots du quotidien (suppression de correspondances erronnees).
- Ajout d'une periode de reinitialisation (cooldown) empechant la repetition abusive d'un meme bruitage a moins de 8 paragraphes d'intervalle.
- Publication directe de l'application au format APK sur GitHub Releases sans compression ZIP pour permettre la mise a jour automatique integree.
- Correction du redimensionnement de l'animation de presentation au demarrage.

## [1.1] - 2026-10-21

### Ajouts
- Integration du processus d'installation assistee (Wizard) a executer une seule fois a la premiere ouverture.
- Verification automatique et silencieuse des mises a jour disponibles via GitHub Releases.
- Amelioration de la stabilite du service de lecture audio en arriere-plan.
