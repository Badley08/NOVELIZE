package com.karlitodev.novelize.parser

import com.karlitodev.novelize.model.DocumentFormat
import com.karlitodev.novelize.model.SpeakerRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * Unit tests verifying document parsing, dialogue speaker detection, and SFX cue identification.
 */
class NovelParserTest {

    private val parser = DefaultNovelParser()

    @Test
    fun parse_splitsChaptersCorrectly() {
        val sampleContent = """
            Chapter 1: The Beginning
            Ren walked into the whispering forest. The blade rested at his hip.
            
            Chapter 2: The Ancient Gate
            "We must hurry," Lyra shouted.
            The sword clashed violently against the stone.
        """.trimIndent()

        val chapters = parser.parse(sampleContent, "Test Novel")

        assertEquals(2, chapters.size)
        assertEquals("Chapter 1: The Beginning", chapters[0].title)
        assertEquals("Chapter 2: The Ancient Gate", chapters[1].title)
    }

    @Test
    fun parse_detectsDialogueSpeakers() {
        val dialogueText = """
            Chapter 1
            "We cannot remain here," said Arthur.
            "I agree with your counsel," replied Morgana.
            The wind howled through the ruined valley.
        """.trimIndent()

        val chapters = parser.parse(dialogueText, "Dialogue Test")
        val paragraphs = chapters[0].paragraphs

        assertTrue(paragraphs.size >= 2)
        // Dialogue paragraphs should have non-narrator roles
        assertTrue(paragraphs[0].speakerRole != SpeakerRole.NARRATOR)
        // Narrative paragraph should have narrator role
        assertEquals(SpeakerRole.NARRATOR, paragraphs.last().speakerRole)
    }

    @Test
    fun parse_tagsSfxCues() {
        val sfxText = """
            Chapter 1
            The warrior parried the strike as the sword clashed with thunderous force.
            A sudden slash divided the mist.
        """.trimIndent()

        val chapters = parser.parse(sfxText, "SFX Test")
        val sentences = chapters[0].paragraphs.flatMap { it.sentences }

        val clashSentence = sentences.firstOrNull { it.sfxCue == "sword_clash" }
        assertNotNull("Should detect sword_clash SFX cue", clashSentence)

        val slashSentence = sentences.firstOrNull { it.sfxCue == "sword_slash" }
        assertNotNull("Should detect sword_slash SFX cue", slashSentence)
    }

    @Test
    fun parseStream_handlesPlainTextStream() {
        val text = "Prologue\nA traveler stepped forward into the tavern."
        val stream = ByteArrayInputStream(text.toByteArray(Charsets.UTF_8))

        val (book, chapters) = parser.parseStream(stream, "my_novel.txt")

        assertEquals(DocumentFormat.TXT, book.format)
        assertEquals("my novel", book.title)
        assertEquals(1, chapters.size)
    }
}
