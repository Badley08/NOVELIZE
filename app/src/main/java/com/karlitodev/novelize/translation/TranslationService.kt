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
 * Offline translation service with embedded bilingual lexicon and light novel vocabulary.
 */
class DefaultTranslationService : TranslationService {

    private val dictionaryEnToFr = mapOf(
        "sanctuary" to "sanctuaire",
        "forest" to "forêt",
        "canopy" to "canopée",
        "rain" to "pluie",
        "thunderstorm" to "orage",
        "lightning" to "foudre / éclair",
        "blade" to "lame",
        "sword" to "épée",
        "dagger" to "dague",
        "magic" to "magie",
        "forbidden" to "interdit / proscrit",
        "seal" to "sceau",
        "fireball" to "boule de feu",
        "purification" to "purification",
        "sigil" to "sceau magique / glyphe",
        "heartbeat" to "battement de cœur",
        "vanguard" to "avant-garde",
        "spire" to "flèche / flèche céleste",
        "grimoire" to "grimoire",
        "inscription" to "inscription",
        "tablet" to "tablette",
        "archaic" to "archaïque",
        "dialect" to "dialecte",
        "eclipse" to "éclipse",
        "relic" to "relique",
        "whispering" to "chuchotant",
        "disturb" to "perturber",
        "surge" to "poussée d'énergie",
        "subterranean" to "souterrain",
        "celestial" to "céleste"
    )

    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): TranslationResult = withContext(Dispatchers.Default) {
        val cleanWord = text.trim().lowercase().replace(Regex("[^a-zA-Z]"), "")
        val translation = dictionaryEnToFr[cleanWord]

        val resultText = if (translation != null) {
            translation
        } else {
            // Contextual translation explanation when word is outside base lexicon
            "« $cleanWord » (On-device lexicon)"
        }

        TranslationResult(
            originalText = text.trim(),
            translatedText = resultText,
            sourceLanguage = sourceLang,
            targetLanguage = targetLang,
            isOfflineDictionary = true
        )
    }
}
