package com.karlitodev.novelize.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.DocumentFormat
import com.karlitodev.novelize.model.Paragraph
import com.karlitodev.novelize.model.Sentence
import com.karlitodev.novelize.model.SpeakerRole
import com.karlitodev.novelize.sfx.SfxCatalog
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * Interface for parsing novel document formats into structured chapters, paragraphs, and sentences.
 */
interface NovelParser {
    fun parse(content: String, title: String): List<Chapter>
    fun parseStream(inputStream: InputStream, fileName: String): Pair<Book, List<Chapter>>
}

/**
 * Universal offline parser supporting EPUB, TXT, DOCX, and PDF novel files
 * with automated dialogue speaker assignment and SFX cues detection.
 */
class DefaultNovelParser(private val context: Context? = null) : NovelParser {

    private val chapterPattern = Pattern.compile(
        "(?i)^(?:chapter|chapitre|episode|prologue|epilogue|act|tome|part)\\s*([0-9ivxlcdm]+)?.*$",
        Pattern.MULTILINE
    )

    private val sentenceSplitPattern = Pattern.compile("(?<=[.!?…])\\s+")

    companion object {
        // Regex patterns for detecting Feminine indicators and names across FR, JP, KR, EN
        private val FEMININE_PATTERN = Pattern.compile(
            """(?i)\b(?:""" +
            // FR Feminine: pronouns, inquit tags, titles, common names
            """elle|la jeune fille|la femme|la dame|la reine|la princesse|madame|mademoiselle|chère|""" +
            """(?:dit|demanda|répondit|cria|chuchota|s'écria|fit|pensa)-t-elle|""" +
            """(?:Marie|Sophie|Camille|Sarah|Emma|Julie|Lucie|Léa|Chloé|Claire|Juliette|Jeanne|Alice|Manon|Charlotte|Inès|Élodie|Aurélie|Mathilde|Audrey|Diane|Hélène|Émilie|Valérie|Margaux|Céline)|""" +
            // EN Feminine: pronouns, inquit tags, titles, common names
            """she|the girl|the woman|the lady|the queen|the princess|mrs\.|miss|ms\.|""" +
            """(?:she said|she asked|she replied|she whispered|she cried|said she|asked she)|""" +
            """(?:Mary|Sarah|Emma|Olivia|Elizabeth|Emily|Jessica|Anna|Grace|Lily|Sophia|Chloe|Mia|Ava|Isabella|Harper|Ella|Amelia|Victoria|Rachel|Diana|Alice|Lucy|Margaret|Rose)|""" +
            // JP Feminine: pronouns, honorifics, endings (-ko, -mi, -ka, -na, -ri, -ne, -sa, -yo, -e), anime/novel heroines
            """kanojo|shoujo|joshi|onna|彼女|少女|女性|""" +
            """\w+-(?:chan|san|sama)|""" +
            """(?:[A-Z][a-z]*(?:ko|mi|ka|na|ri|ne|sa|yo|e))|""" +
            """(?:Sakura|Hinata|Mikasa|Asuka|Nami|Robin|Yui|Tsunade|Erza|Lucy|Kagura|Kagome|Akane|Megumi|Chitoge|Taiga|Saber|Rin|Rem|Ram|Emilia)|""" +
            // KR Feminine: pronouns, honorifics, endings, novel/webtoon names
            """geunyeo|sonyeo|unnie|noona|그녀|소녀|언니|누나|""" +
            """(?:[A-Z][a-z]*(?:-jin|-eun|-ji|-young|-min|-hye|-yoon|-so|-ha|-rin|-won))|""" +
            """(?:Ji-soo|Min-ji|Eun-ji|Seo-yeon|Yu-jin|Hye-jin|So-hee|Chae-young|Ye-ji|Sun-hee|Da-eun|Cha Hae-In|Hae-In|Ye-won|Soo-jin|Bo-ra)""" +
            """)\b"""
        )

        // Regex patterns for detecting Masculine indicators and names across FR, JP, KR, EN
        private val MASCULINE_PATTERN = Pattern.compile(
            """(?i)\b(?:""" +
            // FR Masculine: pronouns, inquit tags, titles, common names
            """il|le jeune homme|l'homme|le monsieur|le roi|le prince|le seigneur|le chevalier|cher|""" +
            """(?:dit|demanda|répondit|cria|chuchota|s'écria|fit|pensa)-t-il|""" +
            """(?:Pierre|Jean|Marc|Thomas|Lucas|Julien|Nicolas|Alexandre|Arthur|Gabriel|Louis|Paul|Antoine|Maxime|Hugo|Théo|Romain|Clément|Sébastien|Damien|Laurent|François|David|Michel)|""" +
            // EN Masculine: pronouns, inquit tags, titles, common names
            """he|the boy|the man|the lord|the king|the prince|the knight|mr\.|sir|""" +
            """(?:he said|he asked|he replied|he whispered|he cried|said he|asked he)|""" +
            """(?:John|James|David|Michael|William|Robert|Arthur|Jack|Harry|George|Thomas|Edward|Alexander|Daniel|Henry|Matthew|Oliver|Samuel|Benjamin|Lucas|Richard|Charles|Joseph)|""" +
            // JP Masculine: pronouns, honorifics, endings (-ro, -to, -shi, -ya, -ke, -ki, -ta, -ru, -o), anime/novel heroes
            """kare|shounen|danshi|otoko|boku|ore|彼|少年|男性|僕|俺|""" +
            """\w+-kun|""" +
            """(?:[A-Z][a-z]*(?:ro|to|shi|ya|ke|ki|ta|ru|o))|""" +
            """(?:Naruto|Sasuke|Ichigo|Luffy|Zoro|Tanjiro|Kenji|Taro|Kazuma|Eren|Levi|Kakashi|Goku|Vegeta|Killua|Gon|Light|Gintoki|Deku|Bakugo|Todoroki)|""" +
            // KR Masculine: pronouns, honorifics, endings, novel/webtoon names
            """geu|sonyeon|oppa|hyung|그|소년|오빠|형|""" +
            """(?:[A-Z][a-z]*(?:-jun|-woo|-ho|-hyun|-seok|-hoon|-tae|-sik))|""" +
            """(?:Jin-woo|Min-jun|Do-hyun|Sung-jin|Jung-woo|Hyun-woo|Ji-hoon|Tae-hyung|Jung-kook|Seok-jin|Nam-joon|Jimin|Kang-tae|Woo-jin|Sang-woo)""" +
            """)\b"""
        )
    }

    override fun parse(content: String, title: String): List<Chapter> {
        val lines = content.lines()
        val chapters = mutableListOf<Chapter>()

        var currentChapterTitle = "Prologue"
        var currentChapterIndex = 1
        val currentParagraphs = mutableListOf<Paragraph>()
        var maleVoiceToggle = false
        var femaleVoiceToggle = false
        var genericGenderToggle = false

        fun flushChapter() {
            if (currentParagraphs.isNotEmpty()) {
                chapters.add(
                    Chapter(
                        id = UUID.randomUUID().toString(),
                        bookId = "",
                        chapterIndex = currentChapterIndex,
                        title = currentChapterTitle,
                        paragraphs = currentParagraphs.toList()
                    )
                )
                currentParagraphs.clear()
                currentChapterIndex++
            }
        }

        var paragraphIndex = 0
        var lastSfxId: String? = null
        var lastSfxParagraphIndex = -100
        var lastAmbientId: String? = null
        var lastAmbientParagraphIndex = -100

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check if line is a chapter header
            val matcher = chapterPattern.matcher(trimmed)
            if (matcher.matches() && trimmed.length < 90) {
                flushChapter()
                currentChapterTitle = trimmed
                paragraphIndex = 0
                continue
            }

            // Determine dialogue speaker using multi-language gender regex (FR, JP, KR, EN)
            val isDialogue = isDialogueParagraph(trimmed)
            val speaker = if (isDialogue) {
                val hasFemale = FEMININE_PATTERN.matcher(trimmed).find()
                val hasMale = MASCULINE_PATTERN.matcher(trimmed).find()

                when {
                    hasFemale && !hasMale -> {
                        femaleVoiceToggle = !femaleVoiceToggle
                        if (femaleVoiceToggle) SpeakerRole.FEMALE_1 else SpeakerRole.FEMALE_2
                    }
                    hasMale && !hasFemale -> {
                        maleVoiceToggle = !maleVoiceToggle
                        if (maleVoiceToggle) SpeakerRole.MALE_1 else SpeakerRole.MALE_2
                    }
                    else -> {
                        // Ambiguous dialogue or alternating conversation: alternate between male & female pairs
                        genericGenderToggle = !genericGenderToggle
                        if (genericGenderToggle) {
                            maleVoiceToggle = !maleVoiceToggle
                            if (maleVoiceToggle) SpeakerRole.MALE_1 else SpeakerRole.MALE_2
                        } else {
                            femaleVoiceToggle = !femaleVoiceToggle
                            if (femaleVoiceToggle) SpeakerRole.FEMALE_1 else SpeakerRole.FEMALE_2
                        }
                    }
                }
            } else {
                SpeakerRole.NARRATOR
            }

            // Split into individual sentences
            val rawSentences = trimmed.split(sentenceSplitPattern)
            var paragraphSfxAssigned = false

            val sentences = rawSentences.mapIndexed { idx, s ->
                val cleanSentence = s.trim()

                // Assign at most 1 SFX per paragraph, and avoid repeating the same SFX too soon
                val sfxCandidate = if (!paragraphSfxAssigned) SfxCatalog.findSfxCue(cleanSentence)?.id else null
                val finalSfx = if (sfxCandidate != null && (sfxCandidate != lastSfxId || (paragraphIndex - lastSfxParagraphIndex) >= 8)) {
                    paragraphSfxAssigned = true
                    lastSfxId = sfxCandidate
                    lastSfxParagraphIndex = paragraphIndex
                    sfxCandidate
                } else null

                // Ambient loop changes should occur sparingly (at least 15 paragraphs apart)
                val ambientCandidate = SfxCatalog.findAmbientCue(cleanSentence)?.id
                val finalAmbient = if (ambientCandidate != null && (ambientCandidate != lastAmbientId) && (paragraphIndex - lastAmbientParagraphIndex >= 15 || lastAmbientId == null)) {
                    lastAmbientId = ambientCandidate
                    lastAmbientParagraphIndex = paragraphIndex
                    ambientCandidate
                } else null

                Sentence(
                    sentenceIndex = idx,
                    text = cleanSentence,
                    sfxCue = finalSfx,
                    ambientCue = finalAmbient
                )
            }.filter { it.text.isNotEmpty() }

            if (sentences.isNotEmpty()) {
                currentParagraphs.add(
                    Paragraph(
                        id = UUID.randomUUID().toString(),
                        paragraphIndex = paragraphIndex++,
                        speakerRole = speaker,
                        sentences = sentences
                    )
                )
            }
        }

        flushChapter()

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    bookId = "",
                    chapterIndex = 1,
                    title = title,
                    paragraphs = emptyList()
                )
            )
        }

        return chapters
    }

    override fun parseStream(inputStream: InputStream, fileName: String): Pair<Book, List<Chapter>> {
        val bookId = UUID.randomUUID().toString()
        val lower = fileName.lowercase()

        return when {
            lower.endsWith(".epub") -> parseEpubStream(inputStream, fileName, bookId)
            lower.endsWith(".pdf") -> parsePdfStream(inputStream, fileName, bookId)
            lower.endsWith(".cbz") -> parseCbzStream(inputStream, fileName, bookId)
            lower.endsWith(".docx") -> parseDocxStream(inputStream, fileName, bookId)
            lower.endsWith(".md") || lower.endsWith(".markdown") -> parseMdStream(inputStream, fileName, bookId)
            else -> parseTxtStream(inputStream, fileName, bookId)
        }
    }

    private fun saveCoverBytes(bookId: String, bytes: ByteArray): String? {
        val ctx = context ?: return null
        return try {
            val coversDir = File(ctx.filesDir, "covers").apply { mkdirs() }
            val coverFile = File(coversDir, "cover_$bookId.jpg")
            coverFile.outputStream().use { it.write(bytes) }
            coverFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun saveCoverBitmap(bookId: String, bitmap: Bitmap): String? {
        val ctx = context ?: return null
        return try {
            val coversDir = File(ctx.filesDir, "covers").apply { mkdirs() }
            val coverFile = File(coversDir, "cover_$bookId.jpg")
            coverFile.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            coverFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTxtStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val fullText = reader.readText()
        val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        val parsedChapters = parse(fullText, title).map { it.copy(bookId = bookId) }

        val book = Book(
            id = bookId,
            title = title,
            author = "Unknown Author",
            coverPath = null,
            format = DocumentFormat.TXT,
            filePath = fileName,
            dominantColorHex = 0xFF6C5CE7,
            totalChapters = parsedChapters.size
        )

        return Pair(book, parsedChapters)
    }

    private fun parseMdStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val fullText = reader.readText()
        val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        val parsedChapters = parse(fullText, title).map { it.copy(bookId = bookId) }

        val book = Book(
            id = bookId,
            title = title,
            author = "Unknown Author",
            coverPath = null,
            format = DocumentFormat.MD,
            filePath = fileName,
            dominantColorHex = 0xFF10B981,
            totalChapters = parsedChapters.size
        )

        return Pair(book, parsedChapters)
    }

    private fun parseDocxStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        val paragraphs = mutableListOf<String>()

        try {
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val xml = readEntryText(zis)
                    val pMatcher = Pattern.compile("<w:p[ >](.*?)</w:p>", Pattern.DOTALL).matcher(xml)
                    while (pMatcher.find()) {
                        val pXml = pMatcher.group(1) ?: ""
                        val tMatcher = Pattern.compile("<w:t[^>]*>(.*?)</w:t>").matcher(pXml)
                        val sb = StringBuilder()
                        while (tMatcher.find()) {
                            sb.append(tMatcher.group(1))
                        }
                        val text = sb.toString().trim()
                        if (text.isNotEmpty()) {
                            paragraphs.add(text)
                        }
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        } catch (_: Exception) {}

        val fullContent = paragraphs.joinToString("\n\n")
        val content = if (fullContent.isNotEmpty()) fullContent else "Document text extracted from $fileName"
        val chapters = parse(content, title).map { it.copy(bookId = bookId) }

        val book = Book(
            id = bookId,
            title = title,
            author = "Unknown Author",
            coverPath = null,
            format = DocumentFormat.DOCX,
            filePath = fileName,
            dominantColorHex = 0xFF3B82F6,
            totalChapters = chapters.size
        )

        return Pair(book, chapters)
    }

    /**
     * Parses a PDF file using PDFBox-Android for text extraction and renders page 0 as cover image.
     */
    private fun parsePdfStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        var extractedText = ""
        var coverPath: String? = null

        val tempFile = File.createTempFile("novelize_pdf_", ".pdf")
        try {
            tempFile.outputStream().use { out -> inputStream.copyTo(out) }

            // Extract cover image from Page 1 using Android native PdfRenderer
            try {
                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    val width = 320
                    val height = (320f * (page.height.toFloat() / page.width.toFloat())).toInt().coerceIn(300, 500)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    coverPath = saveCoverBitmap(bookId, bitmap)
                    bitmap.recycle()
                }
                renderer.close()
                pfd.close()
            } catch (_: Exception) {}

            // Extract text using PDFBox
            if (context != null) {
                try {
                    PDFBoxResourceLoader.init(context)
                } catch (_: Exception) {}
            }

            val document = PDDocument.load(tempFile)
            val stripper = PDFTextStripper().apply {
                sortByPosition = true
                startPage = 1
                endPage = document.numberOfPages
            }
            extractedText = stripper.getText(document) ?: ""
            document.close()
        } catch (_: Exception) {
        } finally {
            try { tempFile.delete() } catch (_: Exception) {}
        }

        val fullContent = if (extractedText.trim().length > 30) {
            extractedText.trim()
        } else {
            "$title\n\nChapter 1\nCe document PDF ne contient pas de texte extractible ou est protégé."
        }

        val chapters = parse(fullContent, title).map { it.copy(bookId = bookId) }
        val book = Book(
            id = bookId,
            title = title,
            author = "Unknown Author",
            coverPath = coverPath,
            format = DocumentFormat.PDF,
            filePath = fileName,
            dominantColorHex = 0xFFEF4444,
            totalChapters = chapters.size
        )
        return Pair(book, chapters)
    }

    /**
     * Parses a CBZ comic file. Extracts image pages, generates cover from first page,
     * and structures chapters as comic pages.
     */
    private fun parseCbzStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        var coverPath: String? = null
        val pageEntries = mutableListOf<String>()

        val tempFile = File.createTempFile("novelize_cbz_", ".cbz")
        try {
            tempFile.outputStream().use { out -> inputStream.copyTo(out) }
            val zip = ZipFile(tempFile)
            val entries = zip.entries().asSequence().toList()

            // Find all image files
            val imageEntries = entries.filter { entry ->
                val name = entry.name.lowercase()
                !entry.isDirectory && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"))
            }.sortedBy { it.name }

            if (imageEntries.isNotEmpty()) {
                // Save first page as cover
                val firstEntry = imageEntries.first()
                val imageBytes = zip.getInputStream(firstEntry).use { it.readBytes() }
                coverPath = saveCoverBytes(bookId, imageBytes)

                imageEntries.forEachIndexed { idx, entry ->
                    pageEntries.add("Page ${idx + 1}: ${entry.name.substringAfterLast("/")}")
                }
            }
            zip.close()
        } catch (_: Exception) {
        } finally {
            try { tempFile.delete() } catch (_: Exception) {}
        }

        val chapters = if (pageEntries.isNotEmpty()) {
            pageEntries.mapIndexed { idx, pageText ->
                Chapter(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    chapterIndex = idx + 1,
                    title = "Page ${idx + 1}",
                    paragraphs = listOf(
                        Paragraph(
                            id = UUID.randomUUID().toString(),
                            paragraphIndex = 0,
                            speakerRole = SpeakerRole.NARRATOR,
                            sentences = listOf(
                                Sentence(
                                    sentenceIndex = 0,
                                    text = pageText
                                )
                            )
                        )
                    )
                )
            }
        } else {
            listOf(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    chapterIndex = 1,
                    title = title,
                    paragraphs = emptyList()
                )
            )
        }

        val book = Book(
            id = bookId,
            title = title,
            author = "Comic Creator",
            coverPath = coverPath,
            format = DocumentFormat.CBZ,
            filePath = fileName,
            dominantColorHex = 0xFFF59E0B,
            totalChapters = chapters.size
        )
        return Pair(book, chapters)
    }

    /**
     * Parses an EPUB file by reading the container, package manifest, spine order, and extracting the cover.
     */
    private fun parseEpubStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        var bookTitle = fileName.substringBeforeLast(".").replace("_", " ")
        var bookAuthor = "Unknown Author"
        var coverPath: String? = null
        val chapters = mutableListOf<Chapter>()

        val tempFile = File.createTempFile("novelize_epub_", ".epub")
        try {
            tempFile.outputStream().use { out -> inputStream.copyTo(out) }
            val zip = ZipFile(tempFile)
            val entries = zip.entries().asSequence().toList()

            // 1. Locate package OPF file via META-INF/container.xml if possible
            var opfPath = ""
            val containerEntry = entries.firstOrNull { it.name.equals("META-INF/container.xml", ignoreCase = true) }
            if (containerEntry != null) {
                val containerXml = zip.getInputStream(containerEntry).bufferedReader(Charsets.UTF_8).readText()
                val match = Regex("full-path=[\"']([^\"']+\\.opf)[\"']", RegexOption.IGNORE_CASE).find(containerXml)
                if (match != null) {
                    opfPath = match.groupValues[1]
                }
            }

            val opfEntry = if (opfPath.isNotEmpty()) {
                entries.firstOrNull { it.name.equals(opfPath, ignoreCase = true) }
            } else {
                entries.firstOrNull { it.name.endsWith(".opf", ignoreCase = true) }
            }

            var opfDir = ""
            val manifestItems = mutableMapOf<String, String>() // id to relative href
            val spineItemrefs = mutableListOf<String>() // list of idrefs in spine order

            if (opfEntry != null) {
                opfDir = if (opfEntry.name.contains("/")) opfEntry.name.substringBeforeLast("/") + "/" else ""
                val opfContent = zip.getInputStream(opfEntry).bufferedReader(Charsets.UTF_8).readText()

                // Extract title
                val titleMatch = Regex("<dc:title[^>]*>([^<]+)</dc:title>", RegexOption.IGNORE_CASE).find(opfContent)
                if (titleMatch != null) {
                    bookTitle = titleMatch.groupValues[1].trim()
                }

                // Extract author
                val authorMatch = Regex("<dc:creator[^>]*>([^<]+)</dc:creator>", RegexOption.IGNORE_CASE).find(opfContent)
                if (authorMatch != null) {
                    bookAuthor = authorMatch.groupValues[1].trim()
                }

                // Manifest items
                val itemMatcher = Pattern.compile("<item\\s+([^>]+)>", Pattern.CASE_INSENSITIVE).matcher(opfContent)
                var coverItemId: String? = null

                while (itemMatcher.find()) {
                    val attrs = itemMatcher.group(1) ?: ""
                    val idMatch = Regex("id=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(attrs)
                    val hrefMatch = Regex("href=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(attrs)
                    val propMatch = Regex("properties=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(attrs)

                    if (idMatch != null && hrefMatch != null) {
                        val id = idMatch.groupValues[1]
                        val href = hrefMatch.groupValues[1]
                        manifestItems[id] = href

                        if (propMatch?.groupValues?.get(1)?.contains("cover-image") == true ||
                            id.equals("cover", ignoreCase = true) ||
                            id.equals("cover-image", ignoreCase = true)
                        ) {
                            coverItemId = id
                        }
                    }
                }

                // Look for <meta name="cover" content="id"/>
                val metaCoverMatch = Regex("<meta\\s+name=[\"']cover[\"']\\s+content=[\"']([^\"']+)[\"']", RegexOption.IGNORE_CASE).find(opfContent)
                if (metaCoverMatch != null) {
                    coverItemId = metaCoverMatch.groupValues[1]
                }

                // Extract cover image bytes if found
                if (coverItemId != null && manifestItems.containsKey(coverItemId)) {
                    val coverHref = manifestItems[coverItemId]!!
                    val coverFullPath = opfDir + coverHref
                    val coverEntry = entries.firstOrNull { it.name.equals(coverFullPath, ignoreCase = true) }
                    if (coverEntry != null) {
                        val bytes = zip.getInputStream(coverEntry).use { it.readBytes() }
                        coverPath = saveCoverBytes(bookId, bytes)
                    }
                }

                // Extract spine reading order
                val itemrefMatcher = Pattern.compile("<itemref\\s+[^>]*idref=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(opfContent)
                while (itemrefMatcher.find()) {
                    itemrefMatcher.group(1)?.let { spineItemrefs.add(it) }
                }
            }

            // Fallback: If no cover was located via OPF, search archive for cover image file
            if (coverPath == null) {
                val fallbackCover = entries.firstOrNull { entry ->
                    val name = entry.name.lowercase()
                    !entry.isDirectory && name.contains("cover") &&
                            (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"))
                }
                if (fallbackCover != null) {
                    val bytes = zip.getInputStream(fallbackCover).use { it.readBytes() }
                    coverPath = saveCoverBytes(bookId, bytes)
                }
            }

            // 2. Read chapters following the spine order
            val chapterEntriesToRead = mutableListOf<ZipEntry>()

            if (spineItemrefs.isNotEmpty()) {
                for (idref in spineItemrefs) {
                    val href = manifestItems[idref] ?: continue
                    val fullHref = opfDir + href
                    val entry = entries.firstOrNull { it.name.equals(fullHref, ignoreCase = true) }
                    if (entry != null && !chapterEntriesToRead.contains(entry)) {
                        chapterEntriesToRead.add(entry)
                    }
                }
            }

            // Fallback: If spine empty or unresolved, gather all XHTML/HTML files
            if (chapterEntriesToRead.isEmpty()) {
                entries.filter { entry ->
                    val name = entry.name.lowercase()
                    !entry.isDirectory && (name.endsWith(".xhtml") || name.endsWith(".html") || name.endsWith(".htm"))
                }.sortedBy { it.name }.forEach { chapterEntriesToRead.add(it) }
            }

            var chapterIndex = 1
            for (entry in chapterEntriesToRead) {
                val html = zip.getInputStream(entry).bufferedReader(Charsets.UTF_8).readText()
                val cleanTitle = extractHtmlTitle(html) ?: "Chapter $chapterIndex"
                val textContent = stripHtmlTags(html)

                if (textContent.length > 40) {
                    val parsed = parse(textContent, cleanTitle)
                    for (ch in parsed) {
                        chapters.add(
                            ch.copy(
                                id = UUID.randomUUID().toString(),
                                bookId = bookId,
                                chapterIndex = chapterIndex++,
                                title = if (ch.title == "Prologue" && cleanTitle != "Prologue") cleanTitle else ch.title
                            )
                        )
                    }
                }
            }

            zip.close()
        } catch (_: Exception) {
        } finally {
            try { tempFile.delete() } catch (_: Exception) {}
        }

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    chapterIndex = 1,
                    title = bookTitle,
                    paragraphs = emptyList()
                )
            )
        }

        val book = Book(
            id = bookId,
            title = bookTitle,
            author = bookAuthor,
            coverPath = coverPath,
            format = DocumentFormat.EPUB,
            filePath = fileName,
            dominantColorHex = 0xFF7C3AED,
            totalChapters = chapters.size
        )

        return Pair(book, chapters)
    }

    private fun readEntryText(zis: ZipInputStream): String {
        val buffer = ByteArray(4096)
        val baos = ByteArrayOutputStream()
        var read: Int
        while (zis.read(buffer).also { read = it } != -1) {
            baos.write(buffer, 0, read)
        }
        return baos.toString("UTF-8")
    }

    private fun extractHtmlTitle(html: String): String? {
        val titleMatch = Regex("<title[^>]*>([^<]+)</title>", RegexOption.IGNORE_CASE).find(html)
        if (titleMatch != null) return titleMatch.groupValues[1].trim()
        val h1Match = Regex("<h1[^>]*>([^<]+)</h1>", RegexOption.IGNORE_CASE).find(html)
        if (h1Match != null) return h1Match.groupValues[1].trim()
        val h2Match = Regex("<h2[^>]*>([^<]+)</h2>", RegexOption.IGNORE_CASE).find(html)
        if (h2Match != null) return h2Match.groupValues[1].trim()
        return null
    }

    private fun stripHtmlTags(html: String): String {
        return html
            .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private fun isDialogueParagraph(text: String): Boolean {
        return text.startsWith("\"") ||
               text.startsWith("“") ||
               text.startsWith("«") ||
               text.startsWith("—") ||
               text.startsWith("- ") ||
               text.endsWith("\"") ||
               text.endsWith("”") ||
               text.endsWith("»")
    }
}
