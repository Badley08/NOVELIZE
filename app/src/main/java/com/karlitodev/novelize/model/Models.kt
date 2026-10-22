package com.karlitodev.novelize.model

import java.util.UUID

/**
 * Supported document formats for novel imports.
 */
enum class DocumentFormat {
    EPUB,
    TXT,
    PDF,
    DOCX
}

/**
 * Represents a novel in the user's library.
 */
data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val coverPath: String? = null,
    val dominantColorHex: Long? = null,
    val format: DocumentFormat,
    val filePath: String,
    val language: String = "en",
    val totalChapters: Int = 1,
    val readingProgressPercent: Float = 0f,
    val isFavorite: Boolean = false,
    val lastReadChapterIndex: Int = 1,
    val lastReadParagraphIndex: Int = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a chapter inside a book.
 */
data class Chapter(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val chapterIndex: Int,
    val title: String,
    val paragraphs: List<Paragraph> = emptyList()
)

/**
 * Speaker roles mapped for multi-voice narration.
 */
enum class SpeakerRole(val displayName: String) {
    NARRATOR("Narrator"),
    HERO("Lead Character"),
    FEMALE_1("Female Voice 1"),
    MALE_1("Male Voice 1"),
    SUPPORTING("Supporting Character")
}

/**
 * Represents a paragraph composed of one or more sentences.
 */
data class Paragraph(
    val id: String = UUID.randomUUID().toString(),
    val paragraphIndex: Int,
    val speakerRole: SpeakerRole = SpeakerRole.NARRATOR,
    val sentences: List<Sentence> = emptyList()
)

/**
 * Represents a single sentence with optional contextual SFX cue.
 */
data class Sentence(
    val sentenceIndex: Int,
    val text: String,
    val sfxCue: String? = null,
    val ambientCue: String? = null
)

/**
 * Tracks the exact reading and narration position down to the word.
 */
data class ReadingProgress(
    val bookId: String,
    val chapterIndex: Int = 0,
    val paragraphIndex: Int = 0,
    val sentenceIndex: Int = 0,
    val wordIndex: Int = 0,
    val lastSpokenWordIndex: Int = 0,
    val manualStopWordIndex: Int? = null,
    val progressPercentage: Float = 0f
)

/**
 * Highlight and annotation models for selected text.
 */
data class TextHighlight(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val chapterIndex: Int,
    val paragraphIndex: Int,
    val sentenceIndex: Int,
    val startOffset: Int,
    val endOffset: Int,
    val colorHex: Long = 0xFFFFEB3B,
    val isUnderline: Boolean = false
)

/**
 * Quiz question generated at the end of a chapter.
 */
data class QuizQuestion(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)
