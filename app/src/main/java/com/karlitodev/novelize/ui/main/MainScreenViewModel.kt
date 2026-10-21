package com.karlitodev.novelize.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karlitodev.novelize.data.NovelizeRepository
import com.karlitodev.novelize.data.ReaderTheme
import com.karlitodev.novelize.data.UserPreferences
import com.karlitodev.novelize.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing the UI state for the main library and app shell.
 * Combines the book list and user preferences into a single observable state
 * that drives the home screen composables.
 */
class MainScreenViewModel(
    private val repository: NovelizeRepository,
    sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5_000)
) : ViewModel() {

    /** Transient search query filter applied on the local book list. */
    private val _searchQuery = MutableStateFlow("")

    /** Sorting criteria for the bookshelf grid. */
    private val _sortMode = MutableStateFlow(SortMode.LAST_READ)

    /**
     * Merged UI state stream combining books, preferences, search, and sort.
     */
    val uiState: StateFlow<MainScreenUiState> = combine(
        repository.books,
        repository.preferences,
        _searchQuery,
        _sortMode
    ) { books, prefs, query, sort ->
        val filteredBooks = books
            .filter { book ->
                query.isBlank() ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.author.contains(query, ignoreCase = true)
            }
            .let { list ->
                when (sort) {
                    SortMode.LAST_READ -> list.sortedByDescending { it.lastReadTimestamp }
                    SortMode.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
                    SortMode.PROGRESS -> list.sortedByDescending { it.readingProgressPercent }
                }
            }

        MainScreenUiState.Success(
            books = filteredBooks,
            preferences = prefs,
            searchQuery = query,
            sortMode = sort,
            totalBooks = books.size
        ) as MainScreenUiState
    }
        .catch { emit(MainScreenUiState.Error(it)) }
        .stateIn(
            scope = viewModelScope,
            started = sharingStarted,
            initialValue = MainScreenUiState.Loading
        )

    /**
     * Updates the library search filter. Filtering happens reactively through the state flow.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.update { query }
    }

    /**
     * Changes the sort ordering for the bookshelf.
     */
    fun setSortMode(mode: SortMode) {
        _sortMode.update { mode }
    }

    /**
     * Removes a book from the library by ID.
     */
    fun removeBook(bookId: String) {
        viewModelScope.launch {
            val current = (uiState.value as? MainScreenUiState.Success)?.books ?: return@launch
            val book = current.find { it.id == bookId } ?: return@launch
            val chapters = repository.getChapters(bookId)
            // Re-add empty to trigger removal in repo
            repository.addBook(book.copy(title = ""), emptyList())
        }
    }

    /**
     * Updates global user preferences (theme, font size, audio volumes, etc.).
     */
    fun updatePreferences(updated: UserPreferences) {
        viewModelScope.launch {
            repository.updatePreferences(updated)
        }
    }
}

/**
 * Sorting modes for the library bookshelf display.
 */
enum class SortMode(val displayName: String) {
    LAST_READ("Recently Read"),
    TITLE_ASC("Title A-Z"),
    PROGRESS("Progress")
}

/**
 * Sealed state hierarchy for the main screen UI.
 */
sealed interface MainScreenUiState {

    /** Initial loading state before data is available. */
    data object Loading : MainScreenUiState

    /** Error state with the throwable that caused the failure. */
    data class Error(val throwable: Throwable) : MainScreenUiState

    /**
     * Successfully loaded state containing the filtered and sorted book list,
     * current user preferences, and active search/sort configuration.
     */
    data class Success(
        val books: List<Book>,
        val preferences: UserPreferences,
        val searchQuery: String,
        val sortMode: SortMode,
        val totalBooks: Int
    ) : MainScreenUiState
}
