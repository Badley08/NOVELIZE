package com.karlitodev.novelize.quiz

import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.QuizQuestion
import java.util.UUID

/**
 * Lightweight offline comprehension quiz builder.
 * Generates quick reading comprehension review questions from chapter passages.
 */
object QuizEngine {

    fun generateQuizForChapter(chapter: Chapter): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val paragraphs = chapter.paragraphs.filter { it.sentences.isNotEmpty() }
        if (paragraphs.size < 2) return emptyList()

        // 1. Dialogue or action question
        val dialogueP = paragraphs.firstOrNull { it.speakerRole != com.karlitodev.novelize.model.SpeakerRole.NARRATOR }
        if (dialogueP != null && dialogueP.sentences.isNotEmpty()) {
            val s = dialogueP.sentences.first().text
            questions.add(
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    question = "Who spoke this line: “${s.take(70)}…”?",
                    options = listOf(dialogueP.speakerRole.displayName, "The Narrator", "A Stranger", "An Ally"),
                    correctOptionIndex = 0,
                    explanation = "This line was spoken by ${dialogueP.speakerRole.displayName} in ${chapter.title}."
                )
            )
        }

        // 2. Setting / Passage recall question
        val firstSentence = paragraphs.first().sentences.firstOrNull()?.text ?: ""
        if (firstSentence.length > 25) {
            questions.add(
                QuizQuestion(
                    id = UUID.randomUUID().toString(),
                    question = "How does this chapter begin?",
                    options = listOf(
                        "“${firstSentence.take(60)}…”",
                        "With a sudden betrayal in the shadows",
                        "In a distant futuristic city",
                        "During a chaotic battlefield retreat"
                    ),
                    correctOptionIndex = 0,
                    explanation = "The opening line establishes the scene for ${chapter.title}."
                )
            )
        }

        return questions
    }
}
