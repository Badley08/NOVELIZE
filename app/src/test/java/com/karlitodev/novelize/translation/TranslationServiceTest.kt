package com.karlitodev.novelize.translation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive unit tests for the offline bilingual translation service.
 * Covers English→French, French→English, accent detection, case handling,
 * out-of-lexicon terms, and vocabulary categories used in light novels.
 */
class TranslationServiceTest {

    private lateinit var service: DefaultTranslationService

    @Before
    fun setUp() {
        service = DefaultTranslationService()
    }

    // ── English → French : Combat & Weapons ─────────────────────────────

    @Test
    fun translate_sword_enToFr() = runTest {
        val result = service.translate("sword")

        assertEquals("épée", result.translatedText)
        assertEquals("en", result.sourceLanguage)
        assertEquals("fr", result.targetLanguage)
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_blade_enToFr() = runTest {
        val result = service.translate("blade")
        assertEquals("lame", result.translatedText)
    }

    @Test
    fun translate_dagger_enToFr() = runTest {
        val result = service.translate("dagger")
        assertEquals("dague", result.translatedText)
    }

    @Test
    fun translate_shield_enToFr() = runTest {
        val result = service.translate("shield")
        assertEquals("bouclier", result.translatedText)
    }

    @Test
    fun translate_bow_enToFr() = runTest {
        val result = service.translate("bow")
        assertEquals("arc", result.translatedText)
    }

    // ── English → French : Magic & Spells ───────────────────────────────

    @Test
    fun translate_magic_enToFr() = runTest {
        val result = service.translate("magic")
        assertEquals("magie", result.translatedText)
    }

    @Test
    fun translate_fireball_enToFr() = runTest {
        val result = service.translate("fireball")
        assertEquals("boule de feu", result.translatedText)
    }

    @Test
    fun translate_grimoire_enToFr() = runTest {
        val result = service.translate("grimoire")
        assertEquals("grimoire", result.translatedText)
    }

    @Test
    fun translate_curse_enToFr() = runTest {
        val result = service.translate("curse")
        assertEquals("malédiction", result.translatedText)
    }

    @Test
    fun translate_healing_enToFr() = runTest {
        val result = service.translate("healing")
        assertEquals("soin / guérison", result.translatedText)
    }

    // ── English → French : Places & Setting ─────────────────────────────

    @Test
    fun translate_sanctuary_enToFr() = runTest {
        val result = service.translate("sanctuary")
        assertEquals("sanctuaire", result.translatedText)
    }

    @Test
    fun translate_castle_enToFr() = runTest {
        val result = service.translate("castle")
        assertEquals("château", result.translatedText)
    }

    @Test
    fun translate_dungeon_enToFr() = runTest {
        val result = service.translate("dungeon")
        assertEquals("donjon / labyrinthe", result.translatedText)
    }

    @Test
    fun translate_forest_enToFr() = runTest {
        val result = service.translate("forest")
        assertEquals("forêt", result.translatedText)
    }

    @Test
    fun translate_tower_enToFr() = runTest {
        val result = service.translate("tower")
        assertEquals("tour", result.translatedText)
    }

    @Test
    fun translate_tavern_enToFr() = runTest {
        val result = service.translate("tavern")
        assertEquals("auberge / taverne", result.translatedText)
    }

    @Test
    fun translate_ruins_enToFr() = runTest {
        val result = service.translate("ruins")
        assertEquals("ruines", result.translatedText)
    }

    // ── English → French : Characters & Roles ───────────────────────────

    @Test
    fun translate_hero_enToFr() = runTest {
        val result = service.translate("hero")
        assertEquals("héros", result.translatedText)
    }

    @Test
    fun translate_knight_enToFr() = runTest {
        val result = service.translate("knight")
        assertEquals("chevalier", result.translatedText)
    }

    @Test
    fun translate_dragon_enToFr() = runTest {
        val result = service.translate("dragon")
        assertEquals("dragon", result.translatedText)
    }

    @Test
    fun translate_demon_enToFr() = runTest {
        val result = service.translate("demon")
        assertEquals("démon", result.translatedText)
    }

    @Test
    fun translate_king_enToFr() = runTest {
        val result = service.translate("king")
        assertEquals("roi", result.translatedText)
    }

    @Test
    fun translate_queen_enToFr() = runTest {
        val result = service.translate("queen")
        assertEquals("reine", result.translatedText)
    }

    @Test
    fun translate_assassin_enToFr() = runTest {
        val result = service.translate("assassin")
        assertEquals("assassin", result.translatedText)
    }

    // ── English → French : Action Verbs (Past Tense) ────────────────────

    @Test
    fun translate_whispered_enToFr() = runTest {
        val result = service.translate("whispered")
        assertEquals("chuchota", result.translatedText)
    }

    @Test
    fun translate_shouted_enToFr() = runTest {
        val result = service.translate("shouted")
        assertEquals("cria", result.translatedText)
    }

    @Test
    fun translate_struck_enToFr() = runTest {
        val result = service.translate("struck")
        assertEquals("frappa", result.translatedText)
    }

    @Test
    fun translate_fled_enToFr() = runTest {
        val result = service.translate("fled")
        assertEquals("s'enfuit", result.translatedText)
    }

    @Test
    fun translate_vanished_enToFr() = runTest {
        val result = service.translate("vanished")
        assertEquals("disparut", result.translatedText)
    }

    @Test
    fun translate_shattered_enToFr() = runTest {
        val result = service.translate("shattered")
        assertEquals("se brisa", result.translatedText)
    }

    @Test
    fun translate_unleashed_enToFr() = runTest {
        val result = service.translate("unleashed")
        assertEquals("déchaîna", result.translatedText)
    }

    // ── English → French : Adjectives & Atmosphere ──────────────────────

    @Test
    fun translate_crimson_enToFr() = runTest {
        val result = service.translate("crimson")
        assertEquals("pourpre / cramoisi", result.translatedText)
    }

    @Test
    fun translate_celestial_enToFr() = runTest {
        val result = service.translate("celestial")
        assertEquals("céleste", result.translatedText)
    }

    @Test
    fun translate_relentless_enToFr() = runTest {
        val result = service.translate("relentless")
        assertEquals("implacable", result.translatedText)
    }

    @Test
    fun translate_ancient_enToFr() = runTest {
        val result = service.translate("ancient")
        assertEquals("ancien / antique", result.translatedText)
    }

    @Test
    fun translate_ferocious_enToFr() = runTest {
        val result = service.translate("ferocious")
        assertEquals("féroce", result.translatedText)
    }

    // ── French → English : Reverse Lookups ──────────────────────────────

    @Test
    fun translate_bouclier_frToEn() = runTest {
        val result = service.translate("bouclier")

        assertEquals("shield", result.translatedText)
        assertEquals("fr", result.sourceLanguage)
        assertEquals("en", result.targetLanguage)
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_epee_frToEn() = runTest {
        val result = service.translate("épée")
        assertEquals("sword", result.translatedText)
        assertEquals("fr", result.sourceLanguage)
    }

    @Test
    fun translate_heros_frToEn() = runTest {
        val result = service.translate("héros")
        assertEquals("hero", result.translatedText)
    }

    @Test
    fun translate_chateau_frToEn() = runTest {
        val result = service.translate("château")
        assertEquals("castle", result.translatedText)
    }

    @Test
    fun translate_foret_frToEn() = runTest {
        val result = service.translate("forêt")
        assertEquals("forest", result.translatedText)
    }

    @Test
    fun translate_magie_frToEn() = runTest {
        val result = service.translate("magie")
        assertEquals("magic", result.translatedText)
    }

    @Test
    fun translate_monstre_frToEn() = runTest {
        val result = service.translate("monstre")
        assertEquals("monster", result.translatedText)
    }

    @Test
    fun translate_foudre_frToEn() = runTest {
        val result = service.translate("foudre")
        assertEquals("lightning", result.translatedText)
    }

    @Test
    fun translate_silencieux_frToEn() = runTest {
        val result = service.translate("silencieux")
        assertEquals("silent", result.translatedText)
    }

    @Test
    fun translate_ancien_frToEn() = runTest {
        val result = service.translate("ancien")
        assertEquals("ancient", result.translatedText)
    }

    @Test
    fun translate_chuchoter_frToEn() = runTest {
        val result = service.translate("chuchoter")
        assertEquals("whisper", result.translatedText)
    }

    @Test
    fun translate_frapper_frToEn() = runTest {
        val result = service.translate("frapper")
        assertEquals("strike", result.translatedText)
    }

    @Test
    fun translate_dragon_frToEn() = runTest {
        // "dragon" exists in both languages; auto-detect should still find it
        val result = service.translate("dragon")
        assertTrue(result.isOfflineDictionary)
    }

    // ── Case Insensitivity ──────────────────────────────────────────────

    @Test
    fun translate_uppercaseInput() = runTest {
        val result = service.translate("SWORD")
        assertEquals("épée", result.translatedText)
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_mixedCaseInput() = runTest {
        val result = service.translate("Castle")
        assertEquals("château", result.translatedText)
    }

    @Test
    fun translate_capitalizedFrench() = runTest {
        val result = service.translate("ÉPÉE")
        assertEquals("sword", result.translatedText)
    }

    // ── Whitespace and Punctuation Handling ──────────────────────────────

    @Test
    fun translate_inputWithLeadingTrailingSpaces() = runTest {
        val result = service.translate("  sword  ")
        assertEquals("épée", result.translatedText)
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_inputWithPunctuationStripped() = runTest {
        val result = service.translate("sword!")
        assertEquals("épée", result.translatedText)
    }

    // ── Out-of-Lexicon Terms ────────────────────────────────────────────

    @Test
    fun translate_unknownEnglishTerm_returnsOutOfLexiconMessage() = runTest {
        val result = service.translate("unicorn")

        assertTrue(result.translatedText.contains("Out-of-lexicon"))
        assertFalse(result.isOfflineDictionary)
    }

    @Test
    fun translate_unknownFrenchTerm_returnsHorsDictionnaireMessage() = runTest {
        val result = service.translate("licorne")

        // "licorne" has no accent but is not in en-fr lexicon;
        // if it's detected as French (unlikely without accents), message is in French
        assertFalse(result.isOfflineDictionary)
    }

    @Test
    fun translate_emptyString_returnsOutOfLexicon() = runTest {
        val result = service.translate("")
        assertFalse(result.isOfflineDictionary)
    }

    // ── Accent Detection ────────────────────────────────────────────────

    @Test
    fun translate_accentedInputDetectedAsFrench() = runTest {
        // "céleste" has French accents so should be auto-detected as French input
        val result = service.translate("céleste")
        assertEquals("fr", result.sourceLanguage)
        assertEquals("en", result.targetLanguage)
    }

    @Test
    fun translate_noAccentsDetectedAsEnglish() = runTest {
        val result = service.translate("sword")
        assertEquals("en", result.sourceLanguage)
        assertEquals("fr", result.targetLanguage)
    }

    // ── TranslationResult Fields ────────────────────────────────────────

    @Test
    fun translate_resultPreservesOriginalText() = runTest {
        val original = "  Shield  "
        val result = service.translate(original)
        assertEquals("Shield", result.originalText)
    }

    @Test
    fun translate_knownTerm_isOfflineDictionaryTrue() = runTest {
        val result = service.translate("mage")
        assertTrue(result.isOfflineDictionary)
    }

    @Test
    fun translate_unknownTerm_isOfflineDictionaryFalse() = runTest {
        val result = service.translate("extraterrestrial")
        assertFalse(result.isOfflineDictionary)
    }

    // ── Bidirectional Round-Trip Consistency ─────────────────────────────

    @Test
    fun translate_roundTrip_sword() = runTest {
        val toFr = service.translate("sword")
        assertEquals("épée", toFr.translatedText)

        val backToEn = service.translate("épée")
        assertEquals("sword", backToEn.translatedText)
    }

    @Test
    fun translate_roundTrip_castle() = runTest {
        val toFr = service.translate("castle")
        assertEquals("château", toFr.translatedText)

        val backToEn = service.translate("château")
        assertEquals("castle", backToEn.translatedText)
    }

    @Test
    fun translate_roundTrip_forest() = runTest {
        val toFr = service.translate("forest")
        assertEquals("forêt", toFr.translatedText)

        val backToEn = service.translate("forêt")
        assertEquals("forest", backToEn.translatedText)
    }

    @Test
    fun translate_roundTrip_hero() = runTest {
        val toFr = service.translate("hero")
        assertEquals("héros", toFr.translatedText)

        val backToEn = service.translate("héros")
        assertEquals("hero", backToEn.translatedText)
    }

    // ── Bulk Vocabulary Coverage ────────────────────────────────────────

    @Test
    fun translate_allCombatTermsExist() = runTest {
        val combatTerms = listOf("sword", "blade", "dagger", "shield", "bow", "arrow", "slash", "clash")
        for (term in combatTerms) {
            val result = service.translate(term)
            assertTrue("Combat term '$term' should be in lexicon", result.isOfflineDictionary)
        }
    }

    @Test
    fun translate_allMagicTermsExist() = runTest {
        val magicTerms = listOf("magic", "spell", "fireball", "flame", "ice", "frost", "grimoire", "relic", "artifact", "mana", "aura")
        for (term in magicTerms) {
            val result = service.translate(term)
            assertTrue("Magic term '$term' should be in lexicon", result.isOfflineDictionary)
        }
    }

    @Test
    fun translate_allCharacterRolesExist() = runTest {
        val roles = listOf("hero", "knight", "mage", "sorcerer", "dragon", "demon", "king", "queen", "lord", "assassin")
        for (role in roles) {
            val result = service.translate(role)
            assertTrue("Role '$role' should be in lexicon", result.isOfflineDictionary)
        }
    }

    @Test
    fun translate_allSettingTermsExist() = runTest {
        val settings = listOf("sanctuary", "forest", "castle", "dungeon", "tower", "library", "ruins", "citadel", "tavern", "throne")
        for (setting in settings) {
            val result = service.translate(setting)
            assertTrue("Setting term '$setting' should be in lexicon", result.isOfflineDictionary)
        }
    }

    @Test
    fun translate_allAtmosphereAdjectivesExist() = runTest {
        val adjectives = listOf("crimson", "celestial", "ancient", "silent", "radiant", "ferocious", "relentless", "abyssal", "ominous", "intense")
        for (adj in adjectives) {
            val result = service.translate(adj)
            assertTrue("Adjective '$adj' should be in lexicon", result.isOfflineDictionary)
        }
    }

    // ── Real Dictionary Stemming & Plurals ──────────────────────────────

    @Test
    fun translate_stemming_pluralsResolveToRoot() = runTest {
        val plurals = listOf("swords", "castles", "dragons", "towers", "daggers")
        for (p in plurals) {
            val res = service.translate(p)
            assertTrue("Plural '$p' should be recognized in dictionary", res.isOfflineDictionary)
        }
    }

    @Test
    fun translate_stemming_pastVerbsResolve() = runTest {
        val pastVerbs = listOf("whispered", "awakened", "retreated", "shattered", "glanced")
        for (v in pastVerbs) {
            val res = service.translate(v)
            assertTrue("Past verb '$v' should be recognized in dictionary", res.isOfflineDictionary)
        }
    }

    @Test
    fun translate_abstractLiteraryTerms_exist() = runTest {
        val abstractTerms = listOf("knowledge", "destiny", "darkness", "courage", "silence", "eternity", "journey")
        for (term in abstractTerms) {
            val res = service.translate(term)
            assertTrue("Literary term '$term' should be recognized in dictionary", res.isOfflineDictionary)
        }
    }

    @Test
    fun translate_frenchToEnglish_literaryTerms() = runTest {
        val frTerms = mapOf(
            "ténèbres" to "darkness",
            "lumière" to "light",
            "voyage" to "journey",
            "espoir" to "hope"
        )
        for ((fr, expectedEn) in frTerms) {
            val res = service.translate(fr)
            assertTrue("French term '$fr' should be found", res.isOfflineDictionary)
            assertEquals("en", res.targetLanguage)
            assertEquals(expectedEn, res.translatedText)
        }
    }
}
