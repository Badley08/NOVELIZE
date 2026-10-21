package com.karlitodev.novelize.quiz

import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.QuizQuestion

/**
 * Public facade for dynamic comprehension quiz generation.
 * Delegates text analysis and question synthesis to MlKitQuizEngine.
 */
object QuizEngine {

    fun generateQuizForChapter(chapter: Chapter): List<QuizQuestion> {
        return MlKitQuizEngine.generateQuizForChapter(chapter)
    }
}
