package com.karlitodev.novelize.translation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Result of an on-device translation lookup.
 */
data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String = "en",
    val targetLanguage: String = "fr",
    val isOfflineDictionary: Boolean = true
)

/**
 * Interface for offline translation services.
 */
interface TranslationService {
    suspend fun translate(text: String, sourceLang: String = "en", targetLang: String = "fr"): TranslationResult
}

/**
 * Comprehensive offline translation service with extensive bidirectional
 * English <-> French lexicon, morphological stemming, and grammatical normalization.
 */
class DefaultTranslationService : TranslationService {

    private val enToFrLexicon: Map<String, String> = mapOf(
        // Combat, Weapons & Armor
        "blade" to "lame",
        "sword" to "épée",
        "swords" to "épées",
        "dagger" to "dague",
        "daggers" to "dagues",
        "shield" to "bouclier",
        "shields" to "boucliers",
        "bow" to "arc",
        "bows" to "arcs",
        "arrow" to "flèche",
        "arrows" to "flèches",
        "axe" to "hache",
        "spear" to "lance",
        "armor" to "armure",
        "gauntlet" to "gantelet",
        "helmet" to "casque",
        "sheath" to "fourreau",
        "scabbard" to "fourreau",
        "hilt" to "garde / poignée",

        // Magic, Spells & Fantasy Elements
        "magic" to "magie",
        "magical" to "magique",
        "spell" to "sort / sortilège",
        "spells" to "sorts",
        "spellbook" to "grimoire",
        "grimoire" to "grimoire",
        "incantation" to "formule magique / incantation",
        "chant" to "incantation",
        "fireball" to "boule de feu",
        "flame" to "flamme",
        "flames" to "flammes",
        "ice" to "glace",
        "frost" to "givre",
        "lightning" to "foudre / éclair",
        "thunder" to "tonnerre",
        "storm" to "tempête",
        "healing" to "soin / guérison",
        "heal" to "guérir / soigner",
        "healed" to "guéri",
        "curse" to "malédiction",
        "cursed" to "maudit",
        "barrier" to "barrière magique",
        "sigil" to "glyphe / sceau magique",
        "seal" to "sceau",
        "sealed" to "scellé",
        "purification" to "purification",
        "purify" to "purifier",
        "forbidden" to "interdit / proscrit",
        "relic" to "relique",
        "artifact" to "artefact",
        "mana" to "mana / énergie magique",
        "aura" to "aura",
        "portal" to "portail",
        "teleport" to "téléportation",
        "teleported" to "téléporté",
        "teleportation" to "téléportation",
        "summon" to "invoquer",
        "summoning" to "invocation",
        "summoned" to "invoqué",
        "enchantment" to "enchantement",
        "potion" to "potion",
        "elixir" to "élixir",

        // Characters, Roles & Creatures
        "hero" to "héros",
        "heroine" to "héroïne",
        "protagonist" to "protagoniste",
        "companion" to "compagnon",
        "companions" to "compagnons",
        "adventurer" to "aventurier",
        "adventurers" to "aventuriers",
        "knight" to "chevalier",
        "knights" to "chevaliers",
        "mage" to "mage / magicien",
        "mages" to "mages",
        "sorcerer" to "sorcier",
        "sorceress" to "sorcière",
        "witch" to "sorcière",
        "wizard" to "magicien",
        "warrior" to "guerrier",
        "warriors" to "guerriers",
        "beast" to "bête / monstre",
        "beasts" to "bêtes",
        "monster" to "monstre",
        "monsters" to "monstres",
        "dragon" to "dragon",
        "dragons" to "dragons",
        "demon" to "démon",
        "demons" to "démons",
        "emperor" to "empereur",
        "empress" to "impératrice",
        "king" to "roi",
        "queen" to "reine",
        "prince" to "prince",
        "princess" to "princesse",
        "lord" to "seigneur",
        "stranger" to "inconnu / étranger",
        "shadow" to "ombre",
        "shadows" to "ombres",
        "assassin" to "assassin",
        "vanguard" to "avant-garde",
        "scholar" to "érudit",
        "master" to "maître",
        "apprentice" to "apprenti",
        "disciple" to "disciple",

        // Places, Settings & Architecture
        "sanctuary" to "sanctuaire",
        "forest" to "forêt",
        "woods" to "bois / forêt",
        "canopy" to "canopée",
        "spire" to "flèche céleste / flèche",
        "castle" to "château",
        "castles" to "châteaux",
        "dungeon" to "donjon / labyrinthe",
        "dungeons" to "donjons",
        "labyrinth" to "labyrinthe",
        "chamber" to "chambre / salle",
        "library" to "bibliothèque",
        "tower" to "tour",
        "towers" to "tours",
        "path" to "chemin / sentier",
        "pathway" to "sentier / chemin",
        "ruins" to "ruines",
        "citadel" to "citadelle",
        "throne" to "trône",
        "sanctum" to "saint des saints / sanctuaire",
        "tavern" to "auberge / taverne",
        "inn" to "auberge",
        "subterranean" to "souterrain",
        "horizon" to "horizon",
        "temple" to "temple",
        "palace" to "palais",
        "village" to "village",
        "valley" to "vallée",
        "mountain" to "montagne",
        "mountains" to "montagnes",
        "cave" to "grotte / caverne",
        "abyss" to "abîme",
        "altar" to "autel",
        "gate" to "porte / portail",
        "gates" to "portes",

        // Action Verbs & Dialogue Tags
        "whisper" to "chuchoter",
        "whispered" to "chuchota",
        "whispering" to "chuchotant",
        "whispers" to "chuchotements",
        "shout" to "crier",
        "shouted" to "cria",
        "shouting" to "criant",
        "draw" to "dégainer / tirer",
        "drew" to "dégaina",
        "drawing" to "dégainant",
        "parry" to "parer",
        "parried" to "para",
        "strike" to "frapper",
        "struck" to "frappa",
        "striking" to "frappant",
        "flee" to "fuir",
        "fled" to "s'enfuit",
        "awaken" to "s'éveiller",
        "awakened" to "s'éveilla",
        "awakening" to "éveil",
        "advance" to "avancer",
        "advanced" to "avança",
        "retreat" to "battre en retraite",
        "retreated" to "battit en retraite",
        "vanish" to "disparaître",
        "vanished" to "disparut",
        "unleash" to "déchaîner",
        "unleashed" to "déchaîna",
        "shatter" to "voler en éclats",
        "shattered" to "se brisa",
        "glance" to "jeter un regard",
        "glanced" to "jeta un regard",
        "murmur" to "murmurer",
        "murmured" to "murmura",
        "gaze" to "contempler / regarder",
        "gazed" to "contempla",
        "stare" to "fixer du regard",
        "stared" to "fixa du regard",
        "clash" to "s'entrechoquer / choc",
        "clashed" to "s'entrechoquèrent",
        "slash" to "entailler / trancher",
        "slashed" to "trancha",
        "pierce" to "percer / transpercer",
        "pierced" to "transperça",
        "defend" to "défendre",
        "defended" to "défendit",
        "attack" to "attaquer",
        "attacked" to "attaqua",
        "destroy" to "détruire",
        "destroyed" to "détruisit",
        "protect" to "protéger",
        "protected" to "protégea",
        "observe" to "observer",
        "observed" to "observa",

        // Descriptive Adjectives & Atmosphere
        "archaic" to "archaïque",
        "celestial" to "céleste",
        "ominous" to "menaçant / de mauvais augure",
        "ancient" to "ancien / antique",
        "silent" to "silencieux",
        "silence" to "silence",
        "radiant" to "rayonnant / éclatant",
        "crimson" to "pourpre / cramoisi",
        "abyssal" to "abyssal",
        "ferocious" to "féroce",
        "fragile" to "fragile",
        "relentless" to "implacable",
        "unwavering" to "inébranlable",
        "subtle" to "subtil",
        "intense" to "intense",
        "dark" to "sombre / obscur",
        "darkness" to "ténèbres / obscurité",
        "bright" to "lumineux / brillant",
        "light" to "lumière",
        "cold" to "froid",
        "warm" to "chaleureux / tiède",
        "eternal" to "éternel",
        "eternity" to "éternité",
        "infinite" to "infini",
        "sacred" to "sacré",
        "divine" to "divin",
        "legendary" to "légendaire",
        "mythical" to "mythique",
        "hidden" to "caché / secret",
        "secret" to "secret",
        "mysterious" to "mystérieux",
        "mystery" to "mystère",
        "courage" to "courage",
        "courageous" to "courageux",
        "brave" to "brave / vaillant",
        "fear" to "peur / effroi",
        "fearful" to "craintif",
        "hopeless" to "désespéré",
        "hope" to "espoir",
        "destiny" to "destin / destinée",
        "fate" to "destin / sort",
        "journey" to "voyage / périple",
        "quest" to "quête",
        "knowledge" to "connaissance / savoir",
        "truth" to "vérité",
        "memory" to "mémoire / souvenir",
        "memories" to "souvenirs",
        "strength" to "force",
        "power" to "puissance / pouvoir",
        "will" to "volonté",
        "resolve" to "détermination"
    )

    private val frToEnLexicon: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for ((en, fr) in enToFrLexicon) {
            val primaryFr = fr.split("/").first().trim().lowercase()
            map[primaryFr] = en
            val fullFr = fr.split("/").map { it.trim().lowercase() }
            for (alt in fullFr) {
                map[alt] = en
            }
        }
        // Additional French literary vocabulary
        map["épée"] = "sword"
        map["épées"] = "swords"
        map["lame"] = "blade"
        map["dague"] = "dagger"
        map["bouclier"] = "shield"
        map["arc"] = "bow"
        map["flèche"] = "arrow"
        map["magie"] = "magic"
        map["sort"] = "spell"
        map["sortilège"] = "curse / spell"
        map["forêt"] = "forest"
        map["donjon"] = "dungeon"
        map["château"] = "castle"
        map["héros"] = "hero"
        map["monstre"] = "monster"
        map["dragon"] = "dragon"
        map["foudre"] = "lightning"
        map["orage"] = "thunderstorm"
        map["pluie"] = "rain"
        map["chuchoter"] = "whisper"
        map["chuchota"] = "whispered"
        map["dégainer"] = "draw (weapon)"
        map["dégaina"] = "drew"
        map["frapper"] = "strike"
        map["frappa"] = "struck"
        map["ancien"] = "ancient"
        map["silencieux"] = "silent"
        map["ténèbres"] = "darkness"
        map["lumière"] = "light"
        map["voyage"] = "journey"
        map["quête"] = "quest"
        map["destin"] = "destiny"
        map["espoir"] = "hope"
        map["peur"] = "fear"
        map["puissance"] = "power"
        map
    }

    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslationResult = withContext(Dispatchers.Default) {
        val rawInput = text.trim()
        val cleanWord = rawInput.lowercase().replace(Regex("[^a-zA-ZÀ-ÿ]"), "")

        // Language detection: French accents or known French lexicon presence
        val isFrenchSource = hasFrenchAccents(cleanWord) || frToEnLexicon.containsKey(cleanWord)
        val actualSource = if (isFrenchSource) "fr" else sourceLang
        val actualTarget = if (isFrenchSource) "en" else targetLang

        val directTranslation = if (isFrenchSource) {
            lookupFrenchToEnglish(cleanWord)
        } else {
            lookupEnglishToFrench(cleanWord)
        }

        val resultText = directTranslation ?: if (isFrenchSource) {
            "« $rawInput » (Vocabulaire hors dictionnaire local)"
        } else {
            "« $rawInput » (Out-of-lexicon term)"
        }

        TranslationResult(
            originalText = rawInput,
            translatedText = resultText,
            sourceLanguage = actualSource,
            targetLanguage = actualTarget,
            isOfflineDictionary = directTranslation != null
        )
    }

    /**
     * Looks up English term with morphological stemming fallback.
     */
    private fun lookupEnglishToFrench(word: String): String? {
        // Direct match
        enToFrLexicon[word]?.let { return it }

        // Stemming: plural -s, -es
        if (word.endsWith("es") && word.length > 3) {
            val stem = word.removeSuffix("es")
            enToFrLexicon[stem]?.let { return it }
        }
        if (word.endsWith("s") && word.length > 2) {
            val stem = word.removeSuffix("s")
            enToFrLexicon[stem]?.let { return it }
        }

        // Stemming: past tense -ed
        if (word.endsWith("ed") && word.length > 3) {
            val stem = word.removeSuffix("ed")
            enToFrLexicon[stem]?.let { return it }
            enToFrLexicon[stem + "e"]?.let { return it }
        }

        // Stemming: continuous -ing
        if (word.endsWith("ing") && word.length > 4) {
            val stem = word.removeSuffix("ing")
            enToFrLexicon[stem]?.let { return it }
            enToFrLexicon[stem + "e"]?.let { return it }
        }

        // Stemming: adverb -ly
        if (word.endsWith("ly") && word.length > 3) {
            val stem = word.removeSuffix("ly")
            enToFrLexicon[stem]?.let { return it }
        }

        return null
    }

    /**
     * Looks up French term with morphological stemming fallback.
     */
    private fun lookupFrenchToEnglish(word: String): String? {
        // Direct match
        frToEnLexicon[word]?.let { return it }

        // Stemming: plurals -s, -x
        if (word.endsWith("s") && word.length > 2) {
            val stem = word.removeSuffix("s")
            frToEnLexicon[stem]?.let { return it }
        }
        if (word.endsWith("x") && word.length > 2) {
            val stem = word.removeSuffix("x")
            frToEnLexicon[stem]?.let { return it }
        }

        // Stemming: past / imperfect verb endings (-ait, -aient, -a, -irent)
        if (word.endsWith("aient") && word.length > 6) {
            val stem = word.removeSuffix("aient") + "er"
            frToEnLexicon[stem]?.let { return it }
        }
        if (word.endsWith("ait") && word.length > 4) {
            val stem = word.removeSuffix("ait") + "er"
            frToEnLexicon[stem]?.let { return it }
        }

        // Check if word appears as a value in enToFrLexicon
        val reverseMatch = enToFrLexicon.entries.firstOrNull { it.value.contains(word) }
        if (reverseMatch != null) {
            return reverseMatch.key
        }

        return null
    }

    private fun hasFrenchAccents(word: String): Boolean {
        return word.any { it in "éèêëàâäîïôöùûüçœæÉÈÊËÀÂÄÎÏÔÖÙÛÜÇ" }
    }
}
