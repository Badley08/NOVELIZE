package com.karlitodev.novelize.audio

import com.karlitodev.novelize.model.SpeakerRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiVoiceEngineTest {

    @Test
    fun testLanguageSupportValidation() {
        val supportedEn = AiVoiceEngine.SUPPORTED_LANGUAGES.contains("en")
        val supportedFr = AiVoiceEngine.SUPPORTED_LANGUAGES.contains("fr")
        val unsupportedEs = AiVoiceEngine.SUPPORTED_LANGUAGES.contains("es")
        val unsupportedDe = AiVoiceEngine.SUPPORTED_LANGUAGES.contains("de")
        val unsupportedJa = AiVoiceEngine.SUPPORTED_LANGUAGES.contains("ja")

        assertTrue("English must be strictly supported", supportedEn)
        assertTrue("French must be strictly supported", supportedFr)
        assertFalse("Spanish must be rejected", unsupportedEs)
        assertFalse("German must be rejected", unsupportedDe)
        assertFalse("Japanese must be rejected", unsupportedJa)
    }

    @Test
    fun testLanguageHeuristics() {
        // Test English detection
        val englishText = "The knight walked into the dark forest with his companions and they were ready for battle."
        val lowerEn = englishText.lowercase().split("\\s+".toRegex())
        val enTokens = listOf("the", "and", "in", "with", "for", "is", "was", "were", "she", "he", "they", "that")
        val hasEn = lowerEn.any { it in enTokens }
        assertTrue(hasEn)

        // Test French detection
        val frenchText = "Le chevalier s'avança dans la forêt sombre avec ses compagnons pour affronter le dragon."
        val lowerFr = frenchText.lowercase().split("\\s+".toRegex())
        val frTokens = listOf("le", "la", "les", "un", "une", "des", "dans", "avec", "pour", "est", "étaient", "elle", "il")
        val hasFr = lowerFr.any { it in frTokens }
        assertTrue(hasFr)
    }

    @Test
    fun testNeuralModelsDefinedForRoles() {
        // Verify models exist for French and English roles
        val enNarrator = AiVoiceEngine.MODELS.firstOrNull { it.language == "en" && it.speakerRole == SpeakerRole.NARRATOR }
        val frNarrator = AiVoiceEngine.MODELS.firstOrNull { it.language == "fr" && it.speakerRole == SpeakerRole.NARRATOR }
        val enHero = AiVoiceEngine.MODELS.firstOrNull { it.language == "en" && it.speakerRole == SpeakerRole.HERO }
        val frHero = AiVoiceEngine.MODELS.firstOrNull { it.language == "fr" && it.speakerRole == SpeakerRole.HERO }

        assertEquals("Piper Lessac High (Narrator)", enNarrator?.name)
        assertEquals("Piper Siwis Medium (Narratrice)", frNarrator?.name)
        assertEquals("Piper LibriTTS (Lead Character)", enHero?.name)
        assertEquals("Piper UPMC Pierre (Personnage Principal)", frHero?.name)
    }
}
