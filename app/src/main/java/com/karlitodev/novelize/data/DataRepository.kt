package com.karlitodev.novelize.data

import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.ReadingProgress
import com.karlitodev.novelize.model.TextHighlight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Unified repository interface for library data access.
 * Provides stream-based access to books, reading progress, and user annotations.
 *
 * Note: The main repository used by navigation and UI is [NovelizeRepository].
 * This interface exists as a lightweight adapter for legacy ViewModel compatibility
 * and unit testing, forwarding calls to [NovelizeRepository] where needed.
 */
interface DataRepository {
    /** Observable stream of all books currently in the library. */
    val data: Flow<List<String>>

    /** Observable stream of library [Book] entries. */
    val books: Flow<List<Book>>

    /** Retrieve the reading progress for a specific book by its ID. */
    suspend fun getReadingProgress(bookId: String): ReadingProgress

    /** Persist reading progress for a specific book. */
    suspend fun saveReadingProgress(progress: ReadingProgress)

    /** Retrieve all highlights for a given book chapter. */
    suspend fun getHighlights(bookId: String, chapterIndex: Int): List<TextHighlight>

    /** Persist a new text highlight or underline annotation. */
    suspend fun addHighlight(highlight: TextHighlight)

    /** Retrieve parsed chapters for a given book. */
    suspend fun getChapters(bookId: String): List<Chapter>

    /** Import a new book and its parsed chapters into the library. */
    suspend fun addBook(book: Book, chapters: List<Chapter>)

    /** Remove a book from the library and clean up its persisted data. */
    suspend fun removeBook(bookId: String)
}

/**
 * Default in-memory implementation of [DataRepository] for testing and previews.
 * Holds books and chapters in memory without touching disk or SharedPreferences.
 */
class DefaultDataRepository : DataRepository {

    private val booksInMemory = mutableListOf<Book>()
    private val chaptersMap = mutableMapOf<String, List<Chapter>>()
    private val progressMap = mutableMapOf<String, ReadingProgress>()
    private val highlightsList = mutableListOf<TextHighlight>()

    override val data: Flow<List<String>> = flow {
        emit(booksInMemory.map { it.title })
    }

    override val books: Flow<List<Book>> = flow {
        emit(booksInMemory.toList())
    }

    override suspend fun getReadingProgress(bookId: String): ReadingProgress {
        return progressMap[bookId] ?: ReadingProgress(bookId = bookId)
    }

    override suspend fun saveReadingProgress(progress: ReadingProgress) {
        progressMap[progress.bookId] = progress
    }

    override suspend fun getHighlights(bookId: String, chapterIndex: Int): List<TextHighlight> {
        return highlightsList.filter { it.bookId == bookId && it.chapterIndex == chapterIndex }
    }

    override suspend fun addHighlight(highlight: TextHighlight) {
        highlightsList.add(highlight)
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        return chaptersMap[bookId] ?: emptyList()
    }

    override suspend fun addBook(book: Book, chapters: List<Chapter>) {
        booksInMemory.add(book)
        chaptersMap[book.id] = chapters
    }

    override suspend fun removeBook(bookId: String) {
        booksInMemory.removeAll { it.id == bookId }
        chaptersMap.remove(bookId)
        progressMap.remove(bookId)
        highlightsList.removeAll { it.bookId == bookId }
    }
}
