package com.karlitodev.novelize

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation destination key for the splash screen.
 * Displays physics-animated logo and requests runtime permissions.
 */
@Serializable
data object SplashNavKey : NavKey

/**
 * Navigation destination key for the first-run setup wizard.
 * Configures initial theme, typography sizing, and presents usage guidelines.
 */
@Serializable
data object WizardNavKey : NavKey

/**
 * Navigation destination key for the library bookshelf screen.
 * Displays all imported novels with cover thumbnails and reading progress.
 */
@Serializable
data object LibraryNavKey : NavKey

/**
 * Navigation destination key for the immersive reader screen.
 * Requires a [bookId] to load the novel content, karaoke playback, and narration controls.
 */
@Serializable
data class ReaderNavKey(val bookId: String) : NavKey

/**
 * Navigation destination key for the settings screen.
 * Manages reader theme, font size, SFX/ambient volumes, and voice packs.
 */
@Serializable
data object SettingsNavKey : NavKey

/**
 * Navigation destination key for the comprehension quiz screen.
 * Generates NLP-based questions from the imported book's content.
 */
@Serializable
data class QuizNavKey(val bookId: String, val chapterIndex: Int) : NavKey

/**
 * Navigation destination key for the offline translation lookup screen.
 * Provides a standalone search over the bidirectional English ⇄ French lexicon.
 */
@Serializable
data object TranslationNavKey : NavKey

/**
 * Navigation destination key for the self-update screen.
 * Checks GitHub Releases for newer APKs and manages in-app download / install.
 */
@Serializable
data object UpdateNavKey : NavKey

/**
 * Navigation destination key for the book detail / metadata screen.
 * Displays cover, author, chapter list, and cumulative reading statistics.
 */
@Serializable
data class BookDetailNavKey(val bookId: String) : NavKey

/**
 * Legacy compatibility key for tests and default back stack entry.
 */
@Serializable
data object Main : NavKey
