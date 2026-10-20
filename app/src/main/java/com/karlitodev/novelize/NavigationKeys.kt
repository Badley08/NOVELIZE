package com.karlitodev.novelize

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Root navigation destination key for splash screen with physics animations and permissions.
 */
@Serializable
data object SplashNavKey : NavKey

/**
 * Root navigation destination key for the first-run wizard.
 */
@Serializable
data object WizardNavKey : NavKey

/**
 * Navigation destination key for the library bookshelf screen.
 */
@Serializable
data object LibraryNavKey : NavKey

/**
 * Navigation destination key for the reader screen.
 */
@Serializable
data class ReaderNavKey(val bookId: String) : NavKey

/**
 * Navigation destination key for the settings screen.
 */
@Serializable
data object SettingsNavKey : NavKey

/**
 * Legacy compatibility key for tests.
 */
@Serializable
data object Main : NavKey
