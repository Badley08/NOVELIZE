package com.karlitodev.novelize.quiz

import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.Paragraph
import com.karlitodev.novelize.model.Sentence
import com.karlitodev.novelize.model.SpeakerRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying dynamic NLP comprehension quiz generation on arbitrary novel chapters.
 */
class QuizEngineTest {

    @Test
    fun generateQuizForChapter_extractsDynamicQuestionsFromText() {
        val chapter = Chapter(
            bookId = "test_book",
            chapterIndex = 1,
            title = "The Obsidian Tower",
            paragraphs = listOf(
                Paragraph(
                    paragraphIndex = 0,
                    speakerRole = SpeakerRole.NARRATOR,
                    sentences = listOf(
                        Sentence(0, "The travelers approached the obsidian gates under heavy rain."),
                        Sentence(1, "Cedric examined the ancient carvings etched into the stone pillars.")
                    )
                ),
                Paragraph(
                    paragraphIndex = 1,
                    speakerRole = SpeakerRole.HERO,
                    sentences = listOf(
                        Sentence(0, "“We must find the hidden key before dawn,” said Cedric with determination.")
                    )
                ),
                Paragraph(
                    paragraphIndex = 2,
                    speakerRole = SpeakerRole.FEMALE_1,
                    sentences = listOf(
                        Sentence(0, "“I sense a dark presence watching from above,” replied Elina cautiously.")
                    )
                ),
                Paragraph(
                    paragraphIndex = 3,
                    speakerRole = SpeakerRole.NARRATOR,
                    sentences = listOf(
                        Sentence(0, "A sudden flash illuminated the dark courtyard before them."),
                        Sentence(1, "The shadows receded as they crossed into the inner sanctum.")
                    )
                )
            )
        )

        val questions = QuizEngine.generateQuizForChapter(chapter)

        assertFalse("Quiz should produce questions for rich chapter content", questions.isEmpty())
        assertTrue("Quiz should produce up to 3 questions", questions.size in 1..3)

        // Ensure correct option index is within options range
        for (q in questions) {
            assertTrue("Options must contain at least 2 choices", q.options.size >= 2)
            assertTrue("Correct index must be within options list", q.correctOptionIndex in q.options.indices)
            assertNotNull("Question explanation should be present", q.explanation)
        }
    }

    @Test
    fun generateQuizForChapter_returnsEmptyForSparseText() {
        val emptyChapter = Chapter(
            bookId = "empty_book",
            chapterIndex = 1,
            title = "Empty",
            paragraphs = emptyList()
        )

        val questions = QuizEngine.generateQuizForChapter(emptyChapter)
        assertTrue("Sparse chapters should return empty question list", questions.isEmpty())
    }
}
