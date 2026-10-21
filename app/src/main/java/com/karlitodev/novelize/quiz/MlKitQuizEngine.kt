package com.karlitodev.novelize.quiz

import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.QuizQuestion
import java.util.UUID
import java.util.regex.Pattern

/**
 * Comprehension question generation engine based on Google ML Kit design patterns.
 * Analyzes named entities (characters, settings), detects document language,
 * and dynamically extracts distractors and questions directly from passage text.
 */
object MlKitQuizEngine {

    private val speechTagRegex = Pattern.compile(
        "(?i)(?:said|replied|asked|shouted|whispered|exclaimed|muttered|cried|dit|repondit|demanda|s'ecria|murmura|affirma|lanca)\\s+([A-ZÀ-ÖØ-ß][a-zà-öø-ÿ]+)|([A-ZÀ-ÖØ-ß][a-zà-öø-ÿ]+)\\s+(?:said|replied|asked|shouted|whispered|exclaimed|muttered|cried|dit|repondit|demanda|s'ecria|murmura|affirma|lanca)"
    )

    private val quoteRegex = Pattern.compile("[\"“«]([^\"”»]{10,120})[\"”»]")

    /**
     * Analyzes a novel chapter and generates a list of multiple-choice comprehension questions.
     */
    fun generateQuizForChapter(chapter: Chapter): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val allSentences = chapter.paragraphs
            .flatMap { it.sentences }
            .map { it.text.trim() }
            .filter { it.length > 20 }

        if (allSentences.size < 4) {
            return emptyList()
        }

        val isFrench = detectIsFrenchLanguage(allSentences)
        val characterEntities = extractNamedEntities(chapter)

        val defaultCharPool = if (characterEntities.size >= 4) {
            characterEntities
        } else {
            val fallback = if (isFrench) {
                listOf("Le Narrateur", "L'Inconnu", "Le Compagnon", "Le Protagoniste")
            } else {
                listOf("The Narrator", "The Stranger", "The Companion", "The Protagonist")
            }
            (characterEntities + fallback).distinct()
        }

        // Question 1: Speaker identification for dialogue quotes
        val quoteQuestion = buildQuoteQuestion(chapter, defaultCharPool, isFrench)
        if (quoteQuestion != null) {
            questions.add(quoteQuestion)
        }

        // Question 2: Factual event recall from chapter sentences
        val eventQuestion = buildEventQuestion(allSentences, isFrench)
        if (eventQuestion != null) {
            questions.add(eventQuestion)
        }

        // Question 3: Chapter opening context
        val openingQuestion = buildOpeningQuestion(allSentences, chapter.title, isFrench)
        if (openingQuestion != null) {
            questions.add(openingQuestion)
        }

        // Question 4: Chapter resolution
        if (questions.size < 3) {
            val closingQuestion = buildClosingQuestion(allSentences, isFrench)
            if (closingQuestion != null) {
                questions.add(closingQuestion)
            }
        }

        return questions.take(3)
    }

    /**
     * Language detection based on grammatical particle frequencies.
     */
    private fun detectIsFrenchLanguage(sentences: List<String>): Boolean {
        val sample = sentences.take(15).joinToString(" ").lowercase()
        val frenchMarkers = listOf(" le ", " la ", " les ", " un ", " une ", " des ", " dans ", " avec ", " pour ", " et ", " est ")
        val englishMarkers = listOf(" the ", " a ", " an ", " in ", " with ", " for ", " and ", " is ", " was ", " were ")

        var frScore = 0
        var enScore = 0

        for (marker in frenchMarkers) {
            if (sample.contains(marker)) frScore++
        }
        for (marker in englishMarkers) {
            if (sample.contains(marker)) enScore++
        }

        return frScore >= enScore
    }

    /**
     * Named entity extraction for characters from dialogue attribution tags and capitalized tokens.
     */
    private fun extractNamedEntities(chapter: Chapter): List<String> {
        val frequencies = mutableMapOf<String, Int>()
        val excludedTokens = setOf(
            "The", "And", "Then", "After", "Before", "When", "While", "Suddenly", "However", "Although",
            "She", "He", "They", "We", "You", "It", "There", "Here", "This", "That", "One", "Two",
            "Le", "La", "Les", "Un", "Une", "Des", "Il", "Elle", "Ils", "Elles", "Nous", "Vous", "Mais", "Alors", "Puis"
        )

        for (p in chapter.paragraphs) {
            for (s in p.sentences) {
                val matcher = speechTagRegex.matcher(s.text)
                while (matcher.find()) {
                    val candidate = (matcher.group(1) ?: matcher.group(2))?.trim()
                    if (candidate != null && candidate.length > 2 && candidate !in excludedTokens) {
                        frequencies[candidate] = (frequencies[candidate] ?: 0) + 4
                    }
                }

                val words = s.text.split(Regex("\\s+"))
                for (i in 1 until words.size) {
                    val clean = words[i].replace(Regex("[^a-zA-ZÀ-ÿ]"), "")
                    if (clean.isNotEmpty() && clean[0].isUpperCase() && clean !in excludedTokens && clean.length > 2) {
                        frequencies[clean] = (frequencies[clean] ?: 0) + 1
                    }
                }
            }
        }

        return frequencies.entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(6)
    }

    private fun buildQuoteQuestion(chapter: Chapter, charPool: List<String>, isFrench: Boolean): QuizQuestion? {
        for (p in chapter.paragraphs) {
            for (s in p.sentences) {
                val m = quoteRegex.matcher(s.text)
                if (m.find()) {
                    val quote = m.group(1)?.trim() ?: continue
                    if (quote.length in 12..100) {
                        val tagMatcher = speechTagRegex.matcher(s.text)
                        val speaker = if (tagMatcher.find()) {
                            tagMatcher.group(1) ?: tagMatcher.group(2) ?: charPool.firstOrNull() ?: "Le Protagoniste"
                        } else {
                            charPool.firstOrNull() ?: "Le Protagoniste"
                        }

                        val distractors = charPool.filter { it != speaker }.shuffled().take(3)
                        if (distractors.size >= 2) {
                            val options = (distractors + speaker).shuffled()
                            val questionText = if (isFrench) {
                                "Dans ce chapitre, quel personnage prononce cette replique ?\n\n« $quote »"
                            } else {
                                "In this chapter, which character spoke the following line?\n\n“$quote”"
                            }
                            val explanationText = if (isFrench) {
                                "Dans le texte du chapitre, cette replique est attribuee a $speaker."
                            } else {
                                "In the text of this chapter, this dialogue is attributed to $speaker."
                            }

                            return QuizQuestion(
                                id = UUID.randomUUID().toString(),
                                question = questionText,
                                options = options,
                                correctOptionIndex = options.indexOf(speaker),
                                explanation = explanationText
                            )
                        }
                    }
                }
            }
        }
        return null
    }

    private fun buildEventQuestion(sentences: List<String>, isFrench: Boolean): QuizQuestion? {
        if (sentences.size < 6) return null
        val targetSentence = sentences[sentences.size / 2]

        // Distractors selected from actual passage sentences
        val distractors = sentences
            .filter { it != targetSentence && it.length in 30..120 }
            .shuffled()
            .take(3)

        if (distractors.size < 3) return null

        val options = (distractors + targetSentence).shuffled()
        val questionText = if (isFrench) {
            "Lequel de ces evenements s'est directement deroule dans ce chapitre ?"
        } else {
            "Which of the following events directly occurred in this chapter?"
        }
        val explanationText = if (isFrench) {
            "Cet evenement est confirme directement dans le recit : « $targetSentence »"
        } else {
            "This event is confirmed directly in the text: “$targetSentence”"
        }

        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            question = questionText,
            options = options,
            correctOptionIndex = options.indexOf(targetSentence),
            explanation = explanationText
        )
    }

    private fun buildOpeningQuestion(sentences: List<String>, chapterTitle: String, isFrench: Boolean): QuizQuestion? {
        val firstSentence = sentences.firstOrNull { it.length in 25..120 } ?: return null
        val otherSentences = sentences.drop(1).filter { it.length in 25..120 }.shuffled().take(3)
        if (otherSentences.size < 3) return null

        val options = (otherSentences + firstSentence).shuffled()
        val questionText = if (isFrench) {
            "Comment debute le passage « $chapterTitle » ?"
        } else {
            "How does “$chapterTitle” begin?"
        }
        val explanationText = if (isFrench) {
            "L'ouverture du chapitre debute par : « $firstSentence »"
        } else {
            "The opening passage begins with: “$firstSentence”"
        }

        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            question = questionText,
            options = options,
            correctOptionIndex = options.indexOf(firstSentence),
            explanation = explanationText
        )
    }

    private fun buildClosingQuestion(sentences: List<String>, isFrench: Boolean): QuizQuestion? {
        val lastSentence = sentences.lastOrNull { it.length in 25..120 } ?: return null
        val otherSentences = sentences.dropLast(1).filter { it.length in 25..120 }.shuffled().take(3)
        if (otherSentences.size < 3) return null

        val options = (otherSentences + lastSentence).shuffled()
        val questionText = if (isFrench) {
            "Quelle phrase vient conclure ce chapitre ?"
        } else {
            "Which sentence concludes this chapter?"
        }
        val explanationText = if (isFrench) {
            "Le chapitre se conclut par : « $lastSentence »"
        } else {
            "The chapter concludes with: “$lastSentence”"
        }

        return QuizQuestion(
            id = UUID.randomUUID().toString(),
            question = questionText,
            options = options,
            correctOptionIndex = options.indexOf(lastSentence),
            explanation = explanationText
        )
    }
}
