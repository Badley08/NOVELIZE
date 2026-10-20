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
 * English ⇄ French lexicon tailored for light novels, fantasy, and narrative prose.
 */
class DefaultTranslationService : TranslationService {

    private val enToFrLexicon = mapOf(
        // Places & Setting
        "sanctuary" to "sanctuaire",
        "forest" to "forêt",
        "woods" to "bois / forêt",
        "canopy" to "canopée",
        "spire" to "flèche céleste / flèche",
        "castle" to "château",
        "dungeon" to "donjon / labyrinthe",
        "chamber" to "chambre / salle",
        "library" to "bibliothèque",
        "tower" to "tour",
        "path" to "chemin / sentier",
        "ruins" to "ruines",
        "citadel" to "citadelle",
        "throne" to "trône",
        "sanctum" to "saint des saints / sanctuaire",
        "tavern" to "auberge / taverne",
        "subterranean" to "souterrain",
        "horizon" to "horizon",

        // Combat, Weapons & Magic
        "blade" to "lame",
        "sword" to "épée",
        "dagger" to "dague",
        "shield" to "bouclier",
        "bow" to "arc",
        "arrow" to "flèche",
        "magic" to "magie",
        "spell" to "sort / sortilège",
        "chant" to "incantation",
        "incantation" to "formule magique",
        "fireball" to "boule de feu",
        "flame" to "flamme",
        "ice" to "glace",
        "frost" to "givre",
        "lightning" to "foudre / éclair",
        "thunder" to "tonnerre",
        "storm" to "tempête",
        "healing" to "soin / guérison",
        "curse" to "malédiction",
        "barrier" to "barrière magique",
        "sigil" to "glyphe / sceau magique",
        "seal" to "sceau",
        "purification" to "purification",
        "forbidden" to "interdit / proscrit",
        "grimoire" to "grimoire",
        "relic" to "relique",
        "artifact" to "artefact",
        "mana" to "mana / énergie magique",
        "aura" to "aura",
        "clash" to "choc / fracas",
        "slash" to "entaille / coup",

        // Characters & Roles
        "hero" to "héros",
        "protagonist" to "protagoniste",
        "companion" to "compagnon",
        "adventurer" to "aventurier",
        "knight" to "chevalier",
        "mage" to "mage / magicien",
        "sorcerer" to "sorcier",
        "beast" to "bête / monstre",
        "monster" to "monstre",
        "dragon" to "dragon",
        "demon" to "démon",
        "emperor" to "empereur",
        "king" to "roi",
        "queen" to "reine",
        "lord" to "seigneur",
        "stranger" to "inconnu / étranger",
        "shadow" to "ombre",
        "assassin" to "assassin",
        "vanguard" to "avant-garde",

        // Action Verbs
        "whisper" to "chuchoter",
        "whispered" to "chuchota",
        "shout" to "crier",
        "shouted" to "cria",
        "draw" to "dégainer / tirer",
        "drew" to "dégaina",
        "parry" to "parer",
        "parried" to "para",
        "strike" to "frapper",
        "struck" to "frappa",
        "flee" to "fuir",
        "fled" to "s'enfuit",
        "awaken" to "s'éveiller",
        "awakened" to "s'éveilla",
        "advance" to "avancer",
        "retreated" to "battit en retraite",
        "vanish" to "disparaître",
        "vanished" to "disparut",
        "unleash" to "déchaîner",
        "unleashed" to "déchaîna",
        "shatter" to "voler en éclats",
        "shattered" to "se brisa",
        "glance" to "jeter un regard",
        "glanced" to "jeta un regard",

        // Adjectives & Atmosphere
        "archaic" to "archaïque",
        "celestial" to "céleste",
        "ominous" to "menaçant / de mauvais augure",
        "ancient" to "ancien / antique",
        "silent" to "silencieux",
        "radiant" to "rayonnant / éclatant",
        "crimson" to "pourpre / cramoisi",
        "abyssal" to "abyssal",
        "ferocious" to "féroce",
        "fragile" to "fragile",
        "relentless" to "implacable",
        "unwavering" to "inébranlable",
        "subtle" to "subtil",
        "intense" to "intense"
    )

    private val frToEnLexicon: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for ((en, fr) in enToFrLexicon) {
            val primaryFr = fr.split("/").first().trim()
            map[primaryFr] = en
        }
        // Additional French literary words
        map["épée"] = "sword"
        map["lame"] = "blade"
        map["magie"] = "magic"
        map["sort"] = "spell"
        map["sortilège"] = "curse / spell"
        map["forêt"] = "forest"
        map["donjon"] = "dungeon"
        map["château"] = "castle"
        map["bouclier"] = "shield"
        map["héros"] = "hero"
        map["monstre"] = "monster"
        map["bête"] = "beast"
        map["dragon"] = "dragon"
        map["foudre"] = "lightning"
        map["orage"] = "thunderstorm"
        map["pluie"] = "rain"
        map["chuchoter"] = "whisper"
        map["dégainer"] = "draw (weapon)"
        map["frapper"] = "strike"
        map["ancien"] = "ancient"
        map["silencieux"] = "silent"
        map
    }

    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslationResult = withContext(Dispatchers.Default) {
        val rawInput = text.trim()
        val cleanWord = rawInput.lowercase().replace(Regex("[^a-zA-ZÀ-ÿ]"), "")

        // Auto-detect direction if needed
        val isFrenchSource = hasFrenchAccents(cleanWord) || frToEnLexicon.containsKey(cleanWord)
        val actualSource = if (isFrenchSource) "fr" else sourceLang
        val actualTarget = if (isFrenchSource) "en" else targetLang

        val translation = if (isFrenchSource) {
            frToEnLexicon[cleanWord] ?: enToFrLexicon.entries.firstOrNull { it.value.contains(cleanWord) }?.key
        } else {
            enToFrLexicon[cleanWord]
        }

        val resultText = translation ?: if (isFrenchSource) {
            "« $rawInput » (Vocabulaire hors dictionnaire local)"
        } else {
            "« $rawInput » (Out-of-lexicon term)"
        }

        TranslationResult(
            originalText = rawInput,
            translatedText = resultText,
            sourceLanguage = actualSource,
            targetLanguage = actualTarget,
            isOfflineDictionary = translation != null
        )
    }

    private fun hasFrenchAccents(word: String): Boolean {
        return word.any { it in "éèêëàâäîïôöùûüçœæÉÈÊËÀÂÄÎÏÔÖÙÛÜÇ" }
    }
}
