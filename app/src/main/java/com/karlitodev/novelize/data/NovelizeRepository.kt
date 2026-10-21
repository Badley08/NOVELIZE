package com.karlitodev.novelize.data

import android.content.Context
import android.content.SharedPreferences
import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.DocumentFormat
import com.karlitodev.novelize.model.Paragraph
import com.karlitodev.novelize.model.ReadingProgress
import com.karlitodev.novelize.model.Sentence
import com.karlitodev.novelize.model.SpeakerRole
import com.karlitodev.novelize.model.TextHighlight
import com.karlitodev.novelize.sample.SampleNovels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Supported reader appearance themes.
 */
enum class ReaderTheme(val displayName: String) {
    LIGHT("Light"),
    SEPIA("Sepia"),
    NIGHT("Night"),
    DYNAMIC("Dynamic Cover")
}

/**
 * Global user preferences for reader and narration.
 */
data class UserPreferences(
    val isWizardCompleted: Boolean = false,
    val theme: ReaderTheme = ReaderTheme.DYNAMIC,
    val fontSizeSp: Float = 18f,
    val lineSpacingMultiplier: Float = 1.35f,
    val sfxEnabled: Boolean = true,
    val sfxVolume: Float = 0.8f,
    val ambientVolume: Float = 0.5f,
    val selectedVoicePack: String = "piper_default",
    val appLanguage: String = "fr"
)

/**
 * Central repository managing library books, chapters, reading progress, and preferences.
 */
interface NovelizeRepository {
    val books: Flow<List<Book>>
    val preferences: Flow<UserPreferences>

    suspend fun getChapters(bookId: String): List<Chapter>
    suspend fun getReadingProgress(bookId: String): ReadingProgress
    suspend fun saveReadingProgress(progress: ReadingProgress)
    suspend fun getHighlights(bookId: String, chapterIndex: Int): List<TextHighlight>
    suspend fun addHighlight(highlight: TextHighlight)
    suspend fun updatePreferences(preferences: UserPreferences)
    suspend fun addBook(book: Book, chapters: List<Chapter>)
}

/**
 * Persistent local repository storing books, chapters, and reading progress
 * in Android application private internal storage and SharedPreferences.
 */
class DefaultNovelizeRepository(
    private val context: Context? = null
) : NovelizeRepository {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("novelize_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _books = MutableStateFlow<List<Book>>(loadInitialBooks())
    override val books: Flow<List<Book>> = _books.asStateFlow()

    private val _preferences = MutableStateFlow(loadInitialPreferences())
    override val preferences: Flow<UserPreferences> = _preferences.asStateFlow()

    private val chapterCache = mutableMapOf<String, List<Chapter>>()
    private val progressCache = mutableMapOf<String, ReadingProgress>()
    private val highlightsCache = mutableListOf<TextHighlight>()

    init {
        // Pre-populate built-in demo novel in cache
        chapterCache[SampleNovels.demoBook.id] = SampleNovels.getDemoChapters()
    }

    private fun loadInitialPreferences(): UserPreferences {
        if (prefs == null) return UserPreferences()
        val completed = prefs.getBoolean("wizard_completed", false)
        val themeName = prefs.getString("reader_theme", ReaderTheme.DYNAMIC.name) ?: ReaderTheme.DYNAMIC.name
        val theme = try { ReaderTheme.valueOf(themeName) } catch (_: Exception) { ReaderTheme.DYNAMIC }
        val fontSize = prefs.getFloat("font_size_sp", 18f)
        val sfx = prefs.getBoolean("sfx_enabled", true)
        val sfxVol = prefs.getFloat("sfx_vol", 0.8f)
        val ambVol = prefs.getFloat("amb_vol", 0.5f)
        val voicePack = prefs.getString("voice_pack", "piper_default") ?: "piper_default"
        val appLang = prefs.getString("app_language", "fr") ?: "fr"

        return UserPreferences(
            isWizardCompleted = completed,
            theme = theme,
            fontSizeSp = fontSize,
            sfxEnabled = sfx,
            sfxVolume = sfxVol,
            ambientVolume = ambVol,
            selectedVoicePack = voicePack,
            appLanguage = appLang
        )
    }

    private fun loadInitialBooks(): List<Book> {
        if (context == null) return listOf(SampleNovels.demoBook)
        val file = File(context.filesDir, "library_books.json")
        if (!file.exists()) return listOf(SampleNovels.demoBook)

        return try {
            val jsonStr = file.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Book>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Book(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        author = obj.getString("author"),
                        coverPath = if (obj.has("coverPath")) obj.getString("coverPath") else null,
                        dominantColorHex = if (obj.has("color")) obj.getLong("color") else 0xFF6C5CE7,
                        format = DocumentFormat.valueOf(obj.getString("format")),
                        filePath = obj.getString("filePath"),
                        language = obj.optString("lang", "en"),
                        totalChapters = obj.getInt("totalChapters"),
                        readingProgressPercent = obj.optDouble("progressPercent", 0.0).toFloat(),
                        lastReadTimestamp = obj.optLong("lastRead", System.currentTimeMillis())
                    )
                )
            }
            if (list.isEmpty()) listOf(SampleNovels.demoBook) else list
        } catch (_: Exception) {
            listOf(SampleNovels.demoBook)
        }
    }

    private fun persistBooks(books: List<Book>) {
        if (context == null) return
        scope.launch {
            try {
                val array = JSONArray()
                for (b in books) {
                    val obj = JSONObject()
                    obj.put("id", b.id)
                    obj.put("title", b.title)
                    obj.put("author", b.author)
                    b.coverPath?.let { obj.put("coverPath", it) }
                    b.dominantColorHex?.let { obj.put("color", it) }
                    obj.put("format", b.format.name)
                    obj.put("filePath", b.filePath)
                    obj.put("lang", b.language)
                    obj.put("totalChapters", b.totalChapters)
                    obj.put("progressPercent", b.readingProgressPercent.toDouble())
                    obj.put("lastRead", b.lastReadTimestamp)
                    array.put(obj)
                }
                File(context.filesDir, "library_books.json").writeText(array.toString())
            } catch (_: Exception) {}
        }
    }

    private fun persistChapters(bookId: String, chapters: List<Chapter>) {
        if (context == null) return
        scope.launch {
            try {
                val file = File(context.filesDir, "chapters_${bookId}.json")
                val root = JSONArray()
                for (ch in chapters) {
                    val chObj = JSONObject()
                    chObj.put("id", ch.id)
                    chObj.put("index", ch.chapterIndex)
                    chObj.put("title", ch.title)
                    val pArray = JSONArray()
                    for (p in ch.paragraphs) {
                        val pObj = JSONObject()
                        pObj.put("id", p.id)
                        pObj.put("index", p.paragraphIndex)
                        pObj.put("speaker", p.speakerRole.name)
                        val sArray = JSONArray()
                        for (s in p.sentences) {
                            val sObj = JSONObject()
                            sObj.put("index", s.sentenceIndex)
                            sObj.put("text", s.text)
                            s.sfxCue?.let { sObj.put("sfx", it) }
                            s.ambientCue?.let { sObj.put("ambient", it) }
                            sArray.put(sObj)
                        }
                        pObj.put("sentences", sArray)
                        pArray.put(pObj)
                    }
                    chObj.put("paragraphs", pArray)
                    root.put(chObj)
                }
                file.writeText(root.toString())
            } catch (_: Exception) {}
        }
    }

    private fun loadChaptersFromDisk(bookId: String): List<Chapter>? {
        if (context == null) return null
        val file = File(context.filesDir, "chapters_${bookId}.json")
        if (!file.exists()) return null

        return try {
            val root = JSONArray(file.readText())
            val chapters = mutableListOf<Chapter>()
            for (i in 0 until root.length()) {
                val chObj = root.getJSONObject(i)
                val pArray = chObj.getJSONArray("paragraphs")
                val paragraphs = mutableListOf<Paragraph>()
                for (j in 0 until pArray.length()) {
                    val pObj = pArray.getJSONObject(j)
                    val sArray = pObj.getJSONArray("sentences")
                    val sentences = mutableListOf<Sentence>()
                    for (k in 0 until sArray.length()) {
                        val sObj = sArray.getJSONObject(k)
                        sentences.add(
                            Sentence(
                                sentenceIndex = sObj.getInt("index"),
                                text = sObj.getString("text"),
                                sfxCue = if (sObj.has("sfx")) sObj.getString("sfx") else null,
                                ambientCue = if (sObj.has("ambient")) sObj.getString("ambient") else null
                            )
                        )
                    }
                    paragraphs.add(
                        Paragraph(
                            id = pObj.getString("id"),
                            paragraphIndex = pObj.getInt("index"),
                            speakerRole = try { SpeakerRole.valueOf(pObj.getString("speaker")) } catch (_: Exception) { SpeakerRole.NARRATOR },
                            sentences = sentences
                        )
                    )
                }
                chapters.add(
                    Chapter(
                        id = chObj.getString("id"),
                        bookId = bookId,
                        chapterIndex = chObj.getInt("index"),
                        title = chObj.getString("title"),
                        paragraphs = paragraphs
                    )
                )
            }
            chapters
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getChapters(bookId: String): List<Chapter> {
        val cached = chapterCache[bookId]
        if (cached != null) return cached

        val disk = loadChaptersFromDisk(bookId)
        if (disk != null && disk.isNotEmpty()) {
            chapterCache[bookId] = disk
            return disk
        }

        return SampleNovels.getDemoChapters()
    }

    override suspend fun getReadingProgress(bookId: String): ReadingProgress {
        val cached = progressCache[bookId]
        if (cached != null) return cached

        if (prefs != null) {
            val key = "progress_$bookId"
            val raw = prefs.getString(key, null)
            if (raw != null) {
                try {
                    val obj = JSONObject(raw)
                    val p = ReadingProgress(
                        bookId = bookId,
                        chapterIndex = obj.optInt("chapterIndex", 1),
                        paragraphIndex = obj.optInt("paragraphIndex", 0),
                        sentenceIndex = obj.optInt("sentenceIndex", 0),
                        wordIndex = obj.optInt("wordIndex", 0),
                        lastSpokenWordIndex = obj.optInt("lastSpokenWordIndex", 0),
                        manualStopWordIndex = if (obj.has("manualStop")) obj.getInt("manualStop") else null,
                        progressPercentage = obj.optDouble("percent", 0.0).toFloat()
                    )
                    progressCache[bookId] = p
                    return p
                } catch (_: Exception) {}
            }
        }

        return ReadingProgress(bookId = bookId)
    }

    override suspend fun saveReadingProgress(progress: ReadingProgress) {
        progressCache[progress.bookId] = progress

        // Persist to SharedPreferences
        prefs?.edit()?.apply {
            val obj = JSONObject().apply {
                put("chapterIndex", progress.chapterIndex)
                put("paragraphIndex", progress.paragraphIndex)
                put("sentenceIndex", progress.sentenceIndex)
                put("wordIndex", progress.wordIndex)
                put("lastSpokenWordIndex", progress.lastSpokenWordIndex)
                progress.manualStopWordIndex?.let { put("manualStop", it) }
                put("percent", progress.progressPercentage.toDouble())
            }
            putString("progress_${progress.bookId}", obj.toString())
            apply()
        }

        // Update last read timestamp on book
        val currentList = _books.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == progress.bookId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                lastReadTimestamp = System.currentTimeMillis(),
                readingProgressPercent = progress.progressPercentage
            )
            _books.value = currentList
            persistBooks(currentList)
        }
    }

    override suspend fun getHighlights(bookId: String, chapterIndex: Int): List<TextHighlight> {
        return highlightsCache.filter { it.bookId == bookId && it.chapterIndex == chapterIndex }
    }

    override suspend fun addHighlight(highlight: TextHighlight) {
        highlightsCache.add(highlight)
    }

    override suspend fun updatePreferences(preferences: UserPreferences) {
        _preferences.value = preferences
        prefs?.edit()?.apply {
            putBoolean("wizard_completed", preferences.isWizardCompleted)
            putString("reader_theme", preferences.theme.name)
            putFloat("font_size_sp", preferences.fontSizeSp)
            putBoolean("sfx_enabled", preferences.sfxEnabled)
            putFloat("sfx_vol", preferences.sfxVolume)
            putFloat("amb_vol", preferences.ambientVolume)
            putString("voice_pack", preferences.selectedVoicePack)
            putString("app_language", preferences.appLanguage)
            apply()
        }
    }

    override suspend fun addBook(book: Book, chapters: List<Chapter>) {
        chapterCache[book.id] = chapters
        val updated = _books.value + book
        _books.value = updated
        persistBooks(updated)
        persistChapters(book.id, chapters)
    }
}
