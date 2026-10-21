package com.karlitodev.novelize

import android.provider.OpenableColumns
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.karlitodev.novelize.audio.NarrationEngine
import com.karlitodev.novelize.data.DefaultNovelizeRepository
import com.karlitodev.novelize.data.NovelizeRepository
import com.karlitodev.novelize.data.UserPreferences
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.parser.DefaultNovelParser
import com.karlitodev.novelize.sample.SampleNovels
import com.karlitodev.novelize.ui.library.LibraryScreen
import com.karlitodev.novelize.ui.reader.ReaderScreen
import com.karlitodev.novelize.ui.settings.SettingsScreen
import com.karlitodev.novelize.ui.splash.SplashScreen
import com.karlitodev.novelize.ui.wizard.WizardScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Root navigation graph for NoveLize.
 * Coordinates screens: Splash (physics + permissions), Wizard, Library, Reader, and Settings.
 */
@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository: NovelizeRepository = remember { DefaultNovelizeRepository(context) }
    val preferences by repository.preferences.collectAsStateWithLifecycle(initialValue = UserPreferences())
    val books by repository.books.collectAsStateWithLifecycle(initialValue = emptyList())

    val narrationEngine = remember { NarrationEngine(context, coroutineScope) }
    val narrationState by narrationEngine.state.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            narrationEngine.release()
        }
    }

    val backStack = rememberNavBackStack(SplashNavKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SplashNavKey> {
                SplashScreen(
                    onSplashComplete = {
                        backStack.clear()
                        if (preferences.isWizardCompleted) {
                            backStack.add(LibraryNavKey)
                        } else {
                            backStack.add(WizardNavKey)
                        }
                    }
                )
            }

            entry<WizardNavKey> {
                WizardScreen(
                    onComplete = { theme, fontSize, appLanguage ->
                        coroutineScope.launch {
                            repository.updatePreferences(
                                preferences.copy(
                                    isWizardCompleted = true,
                                    theme = theme,
                                    fontSizeSp = fontSize,
                                    appLanguage = appLanguage
                                )
                            )
                            backStack.clear()
                            backStack.add(LibraryNavKey)
                        }
                    }
                )
            }

            entry<LibraryNavKey> {
                LibraryScreen(
                    books = books,
                    onBookClick = { book ->
                        backStack.add(ReaderNavKey(book.id))
                    },
                    onImportUri = { uri ->
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    var fileName = "novel.txt"
                                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                        if (cursor.moveToFirst() && nameIndex != -1) {
                                            fileName = cursor.getString(nameIndex) ?: fileName
                                        }
                                    }

                                    context.contentResolver.openInputStream(uri)?.use { stream ->
                                        val parser = DefaultNovelParser(context)
                                        val (importedBook, chapters) = parser.parseStream(stream, fileName)
                                        repository.addBook(importedBook, chapters)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    onImportDemoClick = {
                        coroutineScope.launch {
                            val newBook = SampleNovels.demoBook.copy(
                                id = "demo_${System.currentTimeMillis()}",
                                title = "The Crimson Grimoire (Demo)",
                                author = "A. R. Vance",
                                dominantColorHex = 0xFF7C3AED
                            )
                            repository.addBook(newBook, SampleNovels.getDemoChapters())
                        }
                    },
                    onSettingsClick = {
                        backStack.add(SettingsNavKey)
                    },
                    onPickCover = { bookId, uri ->
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    val coversDir = java.io.File(context.filesDir, "covers").apply { mkdirs() }
                                    val targetFile = java.io.File(coversDir, "cover_${bookId}_${System.currentTimeMillis()}.jpg")
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        targetFile.outputStream().use { output -> input.copyTo(output) }
                                    }
                                    repository.updateBookCover(bookId, targetFile.absolutePath)
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    onDeleteBook = { bookId ->
                        coroutineScope.launch {
                            repository.removeBook(bookId)
                        }
                    },
                    lastSeenVersion = preferences.lastSeenVersion,
                    onAcknowledgeChangelog = { version ->
                        coroutineScope.launch {
                            repository.updatePreferences(preferences.copy(lastSeenVersion = version))
                        }
                    },
                    appLanguage = preferences.appLanguage
                )
            }

            entry<ReaderNavKey> { key ->
                var currentChapter by remember { mutableStateOf<Chapter?>(null) }

                LaunchedEffect(key.bookId) {
                    val book = books.firstOrNull { it.id == key.bookId }
                    val chapters = repository.getChapters(key.bookId)
                    val firstChapter = chapters.firstOrNull() ?: SampleNovels.getDemoChapters().first()
                    currentChapter = firstChapter
                    val progress = repository.getReadingProgress(key.bookId)
                    narrationEngine.setSfxVolume(if (preferences.sfxEnabled) preferences.sfxVolume else 0f)
                    narrationEngine.setAmbientVolume(if (preferences.sfxEnabled) preferences.ambientVolume else 0f)
                    narrationEngine.setChapter(firstChapter, progress, book?.language ?: "en")
                }

                // Automatically persist reading progress as narration advances
                LaunchedEffect(narrationState.currentParagraphIndex, narrationState.currentSentenceIndex, narrationState.currentWordIndex) {
                    currentChapter?.let { ch ->
                        val totalSentences = ch.paragraphs.sumOf { it.sentences.size }.coerceAtLeast(1)
                        val passedSentences = ch.paragraphs.take(narrationState.currentParagraphIndex).sumOf { it.sentences.size } + narrationState.currentSentenceIndex
                        val percent = (passedSentences.toFloat() / totalSentences).coerceIn(0f, 1f)

                        repository.saveReadingProgress(
                            com.karlitodev.novelize.model.ReadingProgress(
                                bookId = key.bookId,
                                chapterIndex = narrationState.currentChapterIndex,
                                paragraphIndex = narrationState.currentParagraphIndex,
                                sentenceIndex = narrationState.currentSentenceIndex,
                                wordIndex = narrationState.currentWordIndex,
                                lastSpokenWordIndex = narrationState.currentWordIndex,
                                progressPercentage = percent
                            )
                        )
                    }
                }

                currentChapter?.let { chapter ->
                    ReaderScreen(
                        chapter = chapter,
                        narrationState = narrationState,
                        theme = preferences.theme,
                        fontSizeSp = preferences.fontSizeSp,
                        onBack = {
                            narrationEngine.pause()
                            backStack.removeLastOrNull()
                        },
                        onPlayPause = { narrationEngine.togglePlayback() },
                        onPreviousSentence = { narrationEngine.previousSentence() },
                        onNextSentence = { narrationEngine.nextSentence() },
                        onSentenceClick = { pIdx, sIdx ->
                            narrationEngine.jumpToPosition(pIdx, sIdx)
                        },
                        onSpeedChange = { speed ->
                            narrationEngine.setPlaybackSpeed(speed)
                        },
                        onSleepTimerSelect = { minutes ->
                            narrationEngine.startSleepTimer(minutes)
                        }
                    )
                }
            }

            entry<SettingsNavKey> {
                SettingsScreen(
                    preferences = preferences,
                    onPreferencesChange = { updated ->
                        coroutineScope.launch {
                            repository.updatePreferences(updated)
                        }
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<Main> {
                LibraryScreen(
                    books = books,
                    onBookClick = { book -> backStack.add(ReaderNavKey(book.id)) },
                    onImportUri = {},
                    onImportDemoClick = {},
                    onSettingsClick = { backStack.add(SettingsNavKey) }
                )
            }
        }
    )
}
