package com.karlitodev.novelize.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.karlitodev.novelize.model.SpeakerRole
import java.io.File

/**
 * Neural voice model metadata descriptor.
 */
data class NeuralVoiceModel(
    val id: String,
    val name: String,
    val language: String, // "en" or "fr"
    val speakerRole: SpeakerRole,
    val sampleRate: Int = 22050,
    val modelFileName: String,
    val configFileName: String
)

/**
 * Result of neural speech synthesis validation.
 */
sealed interface VoiceSynthesisEligibility {
    data object Supported : VoiceSynthesisEligibility
    data class UnsupportedLanguage(val detectedLanguage: String) : VoiceSynthesisEligibility
}

/**
 * On-device Neural AI Speech Synthesis Engine.
 * Strictly limits playback to English and French documents.
 * Replaces standard robotic TTS with neural AI model synthesis (Piper/VITS neural architectures).
 */
class AiVoiceEngine(
    private val context: Context
) {
    companion object {
        val SUPPORTED_LANGUAGES = setOf("en", "fr")

        // Predefined Neural AI Voice models for English and French
        val MODELS = listOf(
            // English Neural Models
            NeuralVoiceModel(
                id = "piper_en_lessac_narrator",
                name = "Piper Lessac High (Narrator)",
                language = "en",
                speakerRole = SpeakerRole.NARRATOR,
                modelFileName = "en_US-lessac-high.onnx",
                configFileName = "en_US-lessac-high.json"
            ),
            NeuralVoiceModel(
                id = "piper_en_libritts_hero",
                name = "Piper LibriTTS (Lead Character)",
                language = "en",
                speakerRole = SpeakerRole.HERO,
                modelFileName = "en_US-libritts-high.onnx",
                configFileName = "en_US-libritts-high.json"
            ),
            NeuralVoiceModel(
                id = "piper_en_female1",
                name = "Piper Amy (Female Voice 1)",
                language = "en",
                speakerRole = SpeakerRole.FEMALE_1,
                modelFileName = "en_US-amy-medium.onnx",
                configFileName = "en_US-amy-medium.json"
            ),
            NeuralVoiceModel(
                id = "piper_en_male1",
                name = "Piper Ryan (Male Voice 1)",
                language = "en",
                speakerRole = SpeakerRole.MALE_1,
                modelFileName = "en_US-ryan-high.onnx",
                configFileName = "en_US-ryan-high.json"
            ),
            NeuralVoiceModel(
                id = "piper_en_supporting",
                name = "Piper Danny (Supporting Character)",
                language = "en",
                speakerRole = SpeakerRole.SUPPORTING,
                modelFileName = "en_US-danny-low.onnx",
                configFileName = "en_US-danny-low.json"
            ),

            // French Neural Models
            NeuralVoiceModel(
                id = "piper_fr_siwis_narrator",
                name = "Piper Siwis Medium (Narratrice)",
                language = "fr",
                speakerRole = SpeakerRole.NARRATOR,
                modelFileName = "fr_FR-siwis-medium.onnx",
                configFileName = "fr_FR-siwis-medium.json"
            ),
            NeuralVoiceModel(
                id = "piper_fr_upmc_hero",
                name = "Piper UPMC Pierre (Personnage Principal)",
                language = "fr",
                speakerRole = SpeakerRole.HERO,
                modelFileName = "fr_FR-upmc-medium.onnx",
                configFileName = "fr_FR-upmc-medium.json"
            ),
            NeuralVoiceModel(
                id = "piper_fr_female1",
                name = "Piper Siwis Claire (Voix Féminine 1)",
                language = "fr",
                speakerRole = SpeakerRole.FEMALE_1,
                modelFileName = "fr_FR-siwis-medium.onnx",
                configFileName = "fr_FR-siwis-medium.json"
            ),
            NeuralVoiceModel(
                id = "piper_fr_male1",
                name = "Piper Gilles (Voix Masculine 1)",
                language = "fr",
                speakerRole = SpeakerRole.MALE_1,
                modelFileName = "fr_FR-gilles-low.onnx",
                configFileName = "fr_FR-gilles-low.json"
            ),
            NeuralVoiceModel(
                id = "piper_fr_supporting",
                name = "Piper Tom (Second Rôle)",
                language = "fr",
                speakerRole = SpeakerRole.SUPPORTING,
                modelFileName = "fr_FR-tom-medium.onnx",
                configFileName = "fr_FR-tom-medium.json"
            )
        )
    }

    /**
     * Storage folder for downloaded neural voice model checkpoints.
     */
    fun getModelsDirectory(): File {
        val dir = File(context.filesDir, "neural_models")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Verifies that the language of the document or passage is strictly English or French.
     * If the language is unsupported, speech synthesis MUST NOT run.
     */
    fun checkEligibility(declaredLanguage: String, sampleText: String): VoiceSynthesisEligibility {
        val cleanLang = declaredLanguage.trim().lowercase().take(2)
        if (cleanLang in SUPPORTED_LANGUAGES) {
            return VoiceSynthesisEligibility.Supported
        }

        // Detect language from text heuristics if not declared
        val detected = detectLanguageHeuristic(sampleText)
        if (detected in SUPPORTED_LANGUAGES) {
            return VoiceSynthesisEligibility.Supported
        }

        return VoiceSynthesisEligibility.UnsupportedLanguage(detected)
    }

    /**
     * Finds the neural voice model assigned to a speaker role and language.
     */
    fun getModelForRole(role: SpeakerRole, language: String): NeuralVoiceModel {
        val targetLang = language.lowercase().take(2)
        return MODELS.firstOrNull { it.language == targetLang && it.speakerRole == role }
            ?: MODELS.first { it.language == targetLang && it.speakerRole == SpeakerRole.NARRATOR }
    }

    /**
     * Lightweight heuristic language detector for French vs English vs other languages.
     */
    fun detectLanguageHeuristic(text: String): String {
        val lower = text.lowercase()

        // Common French words
        val frenchTokens = listOf("le", "la", "les", "un", "une", "des", "dans", "avec", "pour", "est", "étaient", "elle", "il")
        // Common English words
        val englishTokens = listOf("the", "and", "in", "with", "for", "is", "was", "were", "she", "he", "they", "that")

        var frScore = 0
        var enScore = 0

        val words = lower.split("\\s+".toRegex()).take(100)
        for (w in words) {
            if (w in frenchTokens) frScore++
            if (w in englishTokens) enScore++
        }

        return when {
            frScore > enScore && frScore >= 2 -> "fr"
            enScore > frScore && enScore >= 2 -> "en"
            else -> "unknown"
        }
    }
}
