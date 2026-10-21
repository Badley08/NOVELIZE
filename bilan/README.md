# Dossier Bilan & Audit Technique - NoveLize

Ce dossier regroupe les audits, bilans techniques et feuilles de route pour le developpement et la consolidation du projet NoveLize.

## Documents Disponibles

* [Bilan Technique & Ameliorations](file:///home/luberisse/Bureau/NOVELIZE/bilan/bilan_technique_et_ameliorations.md) : Audit approfondi comparant les fonctionnalites annoncees avec la realite du code source Kotlin et Android.
* [Inventaire des Fichiers Audio SFX](file:///home/luberisse/Bureau/NOVELIZE/bilan/sfx_etat_des_lieux.md) : Inventaire complet des 50 fichiers SFX avec [X] pour les presents et [ ] pour les absents.

---

## Resume Executif des Points Critiques

### 1. Fonctionnalites incompletes ou simulees :
* IA Vocale Neuronale Piper/VITS : Utilisation exclusive du TTS standard Android avec modification artificielle du pitch. Absence de moteur neuronal local direct.
* Traduction Hors-Ligne : Dictionnaire reduit a environ 70 mots codes en dur dans une Map.
* Import PDF : Regex naive sur le binaire qui echoue sur la plupart des flux compresses et bascule sur un texte factice.
* Ordre de lecture EPUB : Tri alphabetique simple au lieu du suivi de la balise spine du fichier OPF.
* Dialogue et Locuteurs : Alternance binaire sans extraction d'entites.
* SFX Pack : 18 fichiers presents dans le dossier SFX mais non copies dans res/raw, et 32 absents.
* Quiz dynamique : Distracteurs codes en dur en anglais sans adaptation linguistique.

### 2. Priorite son et mixage :
* Mixage Audio SFX et Narration : Isoler les effets sonores pour qu'ils agissent en fond sonore sans entrer en conflit avec la voix de lecture.

### 3. Ameliorations techniques :
* Migration du stockage JSON monolithique vers Room / SQLite.
* Integration reelle de Google ML Kit pour le traitement de texte et les entites.
