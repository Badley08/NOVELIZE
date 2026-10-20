package com.karlitodev.novelize.translation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying offline bilingual lexicon translation.
 */
class TranslationServiceTest {

    private val service = DefaultTranslationService()

    @Test
    fun translate_englishToFrench_knownTerm() = runTest {
        val result = service.translate("sword")

        assertEquals("épée", result.translatedText)
        assertEquals("en", result.sourceLanguage)
        assertEquals("fr", result.targetLanguage)
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_frenchToEnglish_knownTerm() = runTest {
        val result = service.translate("bouclier")

        assertEquals("shield", result.translatedText)
        assertEquals("fr", result.sourceLanguage)
        assertEquals("en", result.targetLanguage)
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_fantasyVocabulary() = runTest {
        val sanctuaryResult = service.translate("sanctuary")
        assertEquals("sanctuaire", sanctuaryResult.translatedText)

        val dragonResult = service.translate("dragon")
        assertEquals("dragon", dragonResult.translatedText)
    }
}
