package com.karlitodev.novelize.ui.main

import com.karlitodev.novelize.data.NovelizeRepository
import com.karlitodev.novelize.data.ReaderTheme
import com.karlitodev.novelize.data.UserPreferences
import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.DocumentFormat
import com.karlitodev.novelize.model.ReadingProgress
import com.karlitodev.novelize.model.TextHighlight
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying state emissions and user actions in [MainScreenViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeNovelizeRepository
    private lateinit var viewModel: MainScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeNovelizeRepository()
        viewModel = MainScreenViewModel(fakeRepo, kotlinx.coroutines.flow.SharingStarted.Eagerly)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_initiallyLoading() = runTest(testDispatcher) {
        assertEquals(MainScreenUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_successAfterBooksEmitted() = runTest(testDispatcher) {
        fakeRepo.emitBooks(listOf(createSampleBook("1", "The Crimson Grimoire")))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be Success, was $state", state is MainScreenUiState.Success)
        assertEquals(1, (state as MainScreenUiState.Success).books.size)
        assertEquals("The Crimson Grimoire", state.books[0].title)
    }

    @Test
    fun uiState_emptyBooksShowsEmptyList() = runTest(testDispatcher) {
        fakeRepo.emitBooks(emptyList())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MainScreenUiState.Success)
        assertTrue((state as MainScreenUiState.Success).books.isEmpty())
    }

    @Test
    fun searchQuery_filtersBooksByTitle() = runTest(testDispatcher) {
        fakeRepo.emitBooks(
            listOf(
                createSampleBook("1", "The Crimson Grimoire"),
                createSampleBook("2", "Shadow of the Dragon"),
                createSampleBook("3", "Moonlit Blade")
            )
        )
        advanceUntilIdle()

        viewModel.setSearchQuery("dragon")
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals(1, state.books.size)
        assertEquals("Shadow of the Dragon", state.books[0].title)
    }

    @Test
    fun searchQuery_filtersBooksByAuthor() = runTest(testDispatcher) {
        fakeRepo.emitBooks(
            listOf(
                createSampleBook("1", "Book A", author = "Yuki Tanaka"),
                createSampleBook("2", "Book B", author = "John Doe")
            )
        )
        advanceUntilIdle()

        viewModel.setSearchQuery("Yuki")
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals(1, state.books.size)
        assertEquals("Yuki Tanaka", state.books[0].author)
    }

    @Test
    fun searchQuery_emptyReturnsAllBooks() = runTest(testDispatcher) {
        fakeRepo.emitBooks(
            listOf(
                createSampleBook("1", "Book A"),
                createSampleBook("2", "Book B")
            )
        )
        advanceUntilIdle()

        viewModel.setSearchQuery("something")
        advanceUntilIdle()
        viewModel.setSearchQuery("")
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals(2, state.books.size)
    }

    @Test
    fun sortMode_defaultIsLastRead() = runTest(testDispatcher) {
        fakeRepo.emitBooks(emptyList())
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals(SortMode.LAST_READ, state.sortMode)
    }

    @Test
    fun sortMode_titleAscSortsByTitleAlphabetically() = runTest(testDispatcher) {
        fakeRepo.emitBooks(
            listOf(
                createSampleBook("1", "Zephyr Knights"),
                createSampleBook("2", "Arcane Tales"),
                createSampleBook("3", "Moonlit Blade")
            )
        )
        advanceUntilIdle()

        viewModel.setSortMode(SortMode.TITLE_ASC)
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals("Arcane Tales", state.books[0].title)
        assertEquals("Moonlit Blade", state.books[1].title)
        assertEquals("Zephyr Knights", state.books[2].title)
    }

    @Test
    fun sortMode_progressSortsByReadingPercentDescending() = runTest(testDispatcher) {
        fakeRepo.emitBooks(
            listOf(
                createSampleBook("1", "Book A", progress = 0.3f),
                createSampleBook("2", "Book B", progress = 0.9f),
                createSampleBook("3", "Book C", progress = 0.1f)
            )
        )
        advanceUntilIdle()

        viewModel.setSortMode(SortMode.PROGRESS)
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals("Book B", state.books[0].title)
        assertEquals("Book A", state.books[1].title)
        assertEquals("Book C", state.books[2].title)
    }

    @Test
    fun totalBooks_reflectsUnfilteredCount() = runTest(testDispatcher) {
        fakeRepo.emitBooks(
            listOf(
                createSampleBook("1", "Book A"),
                createSampleBook("2", "Book B"),
                createSampleBook("3", "Book C")
            )
        )
        advanceUntilIdle()

        viewModel.setSearchQuery("Book A")
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals(1, state.books.size)
        assertEquals(3, state.totalBooks)
    }

    @Test
    fun preferences_readerTheme_emittedThroughUiState() = runTest(testDispatcher) {
        val updatedPrefs = UserPreferences(theme = ReaderTheme.SEPIA)
        fakeRepo.updatePreferences(updatedPrefs)
        advanceUntilIdle()

        val state = viewModel.uiState.value as MainScreenUiState.Success
        assertEquals(ReaderTheme.SEPIA, state.preferences.theme)
    }

    @Test
    fun uiState_firstOperatorCollectsEmission() = runTest(testDispatcher) {
        fakeRepo.emitBooks(listOf(createSampleBook("1", "First Book")))
        advanceUntilIdle()

        val firstEmission = viewModel.uiState.first()
        assertTrue(firstEmission is MainScreenUiState.Success)
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun createSampleBook(
        id: String,
        title: String,
        author: String = "Test Author",
        progress: Float = 0f
    ) = Book(
        id = id,
        title = title,
        author = author,
        format = DocumentFormat.TXT,
        filePath = "/data/$id.txt",
        readingProgressPercent = progress,
        lastReadTimestamp = System.currentTimeMillis()
    )
}

/**
 * Fake implementation of [NovelizeRepository] for unit testing.
 * Uses MutableStateFlows to simulate reactive data changes.
 */
private class FakeNovelizeRepository : NovelizeRepository {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    override val books: Flow<List<Book>> = _books

    private val _preferences = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = _preferences

    fun emitBooks(list: List<Book>) {
        _books.value = list
    }

    override suspend fun getChapters(bookId: String): List<Chapter> = emptyList()

    override suspend fun getReadingProgress(bookId: String): ReadingProgress =
        ReadingProgress(bookId = bookId)

    override suspend fun saveReadingProgress(progress: ReadingProgress) {}

    override suspend fun getHighlights(bookId: String, chapterIndex: Int): List<TextHighlight> =
        emptyList()

    override suspend fun addHighlight(highlight: TextHighlight) {}

    override suspend fun updatePreferences(preferences: UserPreferences) {
        _preferences.value = preferences
    }

    override suspend fun addBook(book: Book, chapters: List<Chapter>) {
        _books.value = _books.value + book
    }
}
