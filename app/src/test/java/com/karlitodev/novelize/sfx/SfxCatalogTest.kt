package com.karlitodev.novelize.sfx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SfxCatalogTest {

    @Test
    fun testAllFiftyItemsPresent() {
        assertEquals("SfxCatalog must contain exactly 50 items", 50, SfxCatalog.items.size)
    }

    @Test
    fun testItemIdsAndFileNamesAreUnique() {
        val ids = SfxCatalog.items.map { it.id }
        val fileNames = SfxCatalog.items.map { it.fileName }

        assertEquals("All SFX IDs must be unique", 50, ids.toSet().size)
        assertEquals("All SFX file names must be unique", 50, fileNames.toSet().size)
    }

    @Test
    fun testBilingualCueLookup() {
        // Test French cues
        val clashFr = SfxCatalog.findSfxCue("Leurs lames s'entrechoquèrent avec fracas dans la nuit.")
        val fireFr = SfxCatalog.findSfxCue("Il projeta une boule de feu dévastatrice.")

        assertTrue(clashFr != null)
        assertTrue(fireFr != null)

        // Test English cues
        val clashEn = SfxCatalog.findSfxCue("Their swords clashed fiercely in the dark corridor.")
        val fireEn = SfxCatalog.findSfxCue("She hurled a deadly fireball at the beast.")

        assertTrue(clashEn != null)
        assertTrue(fireEn != null)
    }
}
