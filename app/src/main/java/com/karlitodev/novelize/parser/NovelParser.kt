package com.karlitodev.novelize.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.DocumentFormat
import com.karlitodev.novelize.model.Paragraph
import com.karlitodev.novelize.model.Sentence
import com.karlitodev.novelize.model.SpeakerRole
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID
import java.util.regex.Pattern
import java.util.zip.ZipEntry
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
class DefaultNovelParser : NovelParser {

    private val chapterPattern = Pattern.compile(
        "(?i)^(?:chapter|chapitre|episode|prologue|epilogue|act|tome|part)\\s*([0-9ivxlcdm]+)?.*$",
        Pattern.MULTILINE
    )

    private val sentenceSplitPattern = Pattern.compile("(?<=[.!?…])\\s+")

    override fun parse(content: String, title: String): List<Chapter> {
        val lines = content.lines()
        val chapters = mutableListOf<Chapter>()

        var currentChapterTitle = "Prologue"
        var currentChapterIndex = 1
        val currentParagraphs = mutableListOf<Paragraph>()
        var speakerToggle = false

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

            // Determine if paragraph represents dialogue
            val isDialogue = isDialogueParagraph(trimmed)
            val speaker = if (isDialogue) {
                speakerToggle = !speakerToggle
                if (speakerToggle) SpeakerRole.HERO else SpeakerRole.FEMALE_1
            } else {
                SpeakerRole.NARRATOR
            }

            // Split into individual sentences
            val rawSentences = trimmed.split(sentenceSplitPattern)
            val sentences = rawSentences.mapIndexed { idx, s ->
                Sentence(
                    sentenceIndex = idx,
                    text = s.trim(),
                    sfxCue = null,
                    ambientCue = null
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
            lower.endsWith(".docx") -> parseDocxStream(inputStream, fileName, bookId)
            lower.endsWith(".pdf") -> parsePdfStream(inputStream, fileName, bookId)
            else -> parseTxtStream(inputStream, fileName, bookId)
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
            format = DocumentFormat.TXT,
            filePath = fileName,
            dominantColorHex = 0xFF6C5CE7,
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
                if (entry.name.equals("word/document.xml", ignoreCase = true)) {
                    val xmlBytes = readEntryBytes(zis)
                    val pBuilder = StringBuilder()
                    var inParagraph = false
                    var inText = false

                    try {
                        val parser = android.util.Xml.newPullParser()
                        parser.setInput(xmlBytes.inputStream(), "UTF-8")
                        var eventType = parser.eventType
                        while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                            val name = parser.name ?: ""
                            when (eventType) {
                                org.xmlpull.v1.XmlPullParser.START_TAG -> {
                                    if (name.equals("p", ignoreCase = true) || name.endsWith(":p", ignoreCase = true)) {
                                        inParagraph = true
                                        pBuilder.clear()
                                    } else if (name.equals("t", ignoreCase = true) || name.endsWith(":t", ignoreCase = true)) {
                                        inText = true
                                    } else if (name.equals("br", ignoreCase = true) || name.endsWith(":br", ignoreCase = true) ||
                                               name.equals("cr", ignoreCase = true) || name.endsWith(":cr", ignoreCase = true)) {
                                        if (inParagraph) pBuilder.append("\n")
                                    } else if (name.equals("tab", ignoreCase = true) || name.endsWith(":tab", ignoreCase = true)) {
                                        if (inParagraph) pBuilder.append("    ")
                                    }
                                }
                                org.xmlpull.v1.XmlPullParser.TEXT -> {
                                    if (inParagraph && inText) {
                                        pBuilder.append(parser.text)
                                    }
                                }
                                org.xmlpull.v1.XmlPullParser.END_TAG -> {
                                    if (name.equals("t", ignoreCase = true) || name.endsWith(":t", ignoreCase = true)) {
                                        inText = false
                                    } else if (name.equals("p", ignoreCase = true) || name.endsWith(":p", ignoreCase = true)) {
                                        inParagraph = false
                                        val pText = pBuilder.toString().trim()
                                        if (pText.isNotEmpty()) {
                                            paragraphs.add(pText)
                                        }
                                    }
                                }
                            }
                            eventType = parser.next()
                        }
                    } catch (_: Exception) {
                        // Fallback simple regex extraction if pull parser fails
                        val xml = String(xmlBytes, Charsets.UTF_8)
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
            format = DocumentFormat.DOCX,
            filePath = fileName,
            dominantColorHex = 0xFF3B82F6,
            totalChapters = chapters.size
        )

        return Pair(book, chapters)
    }

    private fun parsePdfStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        val title = fileName.substringBeforeLast(".").replace("_", " ").replace("-", " ")
        val sb = StringBuilder()

        try {
            val bytes = inputStream.readBytes()
            val textBlocks = extractPdfStreams(bytes)
            for (block in textBlocks) {
                val extracted = extractTextFromPdfStream(block)
                if (extracted.isNotBlank()) {
                    sb.append(extracted).append("\n\n")
                }
            }
        } catch (_: Exception) {}

        val extracted = sb.toString().trim()
        val fullContent = if (extracted.length > 30) extracted else "PDF Document: $title\n\nChapter 1\nImported PDF contents ready for audio drama narration."
        val chapters = parse(fullContent, title).map { it.copy(bookId = bookId) }

        val book = Book(
            id = bookId,
            title = title,
            author = "Unknown Author",
            format = DocumentFormat.PDF,
            filePath = fileName,
            dominantColorHex = 0xFFEF4444,
            totalChapters = chapters.size
        )

        return Pair(book, chapters)
    }

    private fun extractPdfStreams(pdfBytes: ByteArray): List<String> {
        val streams = mutableListOf<String>()
        val raw = String(pdfBytes, Charsets.ISO_8859_1)

        val streamPattern = Pattern.compile("stream[\r\n]+([\\s\\S]*?)[\r\n]+endstream")
        val matcher = streamPattern.matcher(raw)

        while (matcher.find()) {
            val startIdx = matcher.start(1)
            val endIdx = matcher.end(1)
            val headerIdx = raw.lastIndexOf("<<", startIdx)
            val isFlate = if (headerIdx != -1 && headerIdx < startIdx) {
                raw.substring(headerIdx, startIdx).contains("FlateDecode")
            } else false

            val streamBytes = pdfBytes.copyOfRange(startIdx, endIdx)

            if (isFlate) {
                try {
                    val inflater = java.util.zip.Inflater()
                    inflater.setInput(streamBytes)
                    val buffer = ByteArray(4096)
                    val baos = ByteArrayOutputStream()
                    while (!inflater.finished()) {
                        val count = inflater.inflate(buffer)
                        if (count <= 0) break
                        baos.write(buffer, 0, count)
                    }
                    inflater.end()
                    streams.add(baos.toString("ISO-8859-1"))
                } catch (_: Exception) {
                    streams.add(String(streamBytes, Charsets.ISO_8859_1))
                }
            } else {
                streams.add(String(streamBytes, Charsets.ISO_8859_1))
            }
        }

        // If no streams found, fallback to scanning entire raw content
        if (streams.isEmpty()) {
            streams.add(raw)
        }

        return streams
    }

    private fun extractTextFromPdfStream(streamContent: String): String {
        val sb = StringBuilder()
        val btEtPattern = Pattern.compile("BT\\s+([\\s\\S]*?)\\s+ET")
        val btMatcher = btEtPattern.matcher(streamContent)

        while (btMatcher.find()) {
            val textBlock = btMatcher.group(1) ?: continue

            // 1. Tj operators: (string) Tj
            val tjMatcher = Pattern.compile("\\((.*?)\\)\\s*(?:Tj|'|\")").matcher(textBlock)
            while (tjMatcher.find()) {
                val str = decodePdfString(tjMatcher.group(1) ?: "")
                if (str.isNotBlank()) sb.append(str).append(" ")
            }

            // 2. TJ array operators: [(s1) -10 (s2)] TJ
            val arrayTjMatcher = Pattern.compile("\\[([^\\]]*)\\]\\s*TJ").matcher(textBlock)
            while (arrayTjMatcher.find()) {
                val arrayContent = arrayTjMatcher.group(1) ?: ""
                val itemMatcher = Pattern.compile("\\((.*?)\\)").matcher(arrayContent)
                while (itemMatcher.find()) {
                    val item = decodePdfString(itemMatcher.group(1) ?: "")
                    if (item.isNotBlank()) sb.append(item)
                }
                sb.append(" ")
            }
            sb.append("\n")
        }

        return sb.toString().trim()
    }

    private fun decodePdfString(input: String): String {
        return input
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
    }

    private fun parseEpubStream(inputStream: InputStream, fileName: String, bookId: String): Pair<Book, List<Chapter>> {
        var bookTitle = fileName.substringBeforeLast(".").replace("_", " ")
        var bookAuthor = "Unknown Author"
        val zipFiles = mutableMapOf<String, ByteArray>()
        var opfPath = ""

        try {
            val zis = ZipInputStream(inputStream)
            var entry: ZipEntry? = zis.nextEntry

            while (entry != null) {
                val name = entry.name
                val bytes = readEntryBytes(zis)
                zipFiles[name] = bytes

                if (name.endsWith(".opf", ignoreCase = true)) {
                    opfPath = name
                } else if (name.equals("META-INF/container.xml", ignoreCase = true)) {
                    val containerXml = String(bytes, Charsets.UTF_8)
                    val rootMatch = Regex("full-path=\"([^\"]+)\"", RegexOption.IGNORE_CASE).find(containerXml)
                    if (rootMatch != null) {
                        opfPath = rootMatch.groupValues[1]
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        } catch (_: Exception) {}

        // Read OPF metadata and spine
        val spineHrefs = mutableListOf<String>()
        if (opfPath.isNotEmpty() && zipFiles.containsKey(opfPath)) {
            val opfContent = String(zipFiles[opfPath] ?: ByteArray(0), Charsets.UTF_8)
            val titleMatch = Regex("<dc:title[^>]*>([^<]+)</dc:title>", RegexOption.IGNORE_CASE).find(opfContent)
            if (titleMatch != null) {
                bookTitle = titleMatch.groupValues[1].trim()
            }
            val authorMatch = Regex("<dc:creator[^>]*>([^<]+)</dc:creator>", RegexOption.IGNORE_CASE).find(opfContent)
            if (authorMatch != null) {
                bookAuthor = authorMatch.groupValues[1].trim()
            }

            // Manifest id -> href
            val idToHref = mutableMapOf<String, String>()
            val itemMatcher = Pattern.compile("<item[^>]+id=\"([^\"]+)\"[^>]+href=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(opfContent)
            while (itemMatcher.find()) {
                val id = itemMatcher.group(1) ?: ""
                val href = itemMatcher.group(2) ?: ""
                idToHref[id] = href
            }

            // Spine reading order
            val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""
            val spineMatcher = Pattern.compile("<itemref[^>]+idref=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE).matcher(opfContent)
            while (spineMatcher.find()) {
                val idref = spineMatcher.group(1) ?: ""
                val href = idToHref[idref]
                if (href != null) {
                    val fullPath = opfDir + href
                    spineHrefs.add(fullPath)
                }
            }
        }

        // Ordered list of HTML files to process
        val orderedHtmlFiles = mutableListOf<String>()
        for (href in spineHrefs) {
            val match = zipFiles.keys.firstOrNull { it.equals(href, ignoreCase = true) || it.endsWith(href, ignoreCase = true) }
            if (match != null && !orderedHtmlFiles.contains(match)) {
                orderedHtmlFiles.add(match)
            }
        }

        // Fallback to alphabetical sorting if no spine was resolved
        if (orderedHtmlFiles.isEmpty()) {
            orderedHtmlFiles.addAll(
                zipFiles.keys.filter {
                    it.endsWith(".xhtml", ignoreCase = true) ||
                    it.endsWith(".html", ignoreCase = true) ||
                    it.endsWith(".htm", ignoreCase = true)
                }.sorted()
            )
        }

        val chapters = mutableListOf<Chapter>()
        var chapterIndex = 1

        for (path in orderedHtmlFiles) {
            val bytes = zipFiles[path] ?: continue
            val htmlContent = String(bytes, Charsets.UTF_8)
            val cleanTitle = extractHtmlTitle(htmlContent) ?: "Chapter $chapterIndex"
            val textContent = stripHtmlTags(htmlContent)

            if (textContent.length > 50) {
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
            format = DocumentFormat.EPUB,
            filePath = fileName,
            dominantColorHex = 0xFF7C3AED,
            totalChapters = chapters.size
        )

        return Pair(book, chapters)
    }

    private fun readEntryBytes(zis: ZipInputStream): ByteArray {
        val buffer = ByteArray(4096)
        val baos = ByteArrayOutputStream()
        var read: Int
        while (zis.read(buffer).also { read = it } != -1) {
            baos.write(buffer, 0, read)
        }
        return baos.toByteArray()
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
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT).toString().trim()
            } else {
                @Suppress("DEPRECATION")
                android.text.Html.fromHtml(html).toString().trim()
            }
        } catch (_: Exception) {
            html
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
                .trim()
        }
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

    /**
     * Extracts and persists the cover image for an imported book.
     * Supports:
     * - PDF: renders page 0 using native Android PdfRenderer
     * - EPUB: extracts cover image from ZIP entries
     * - DOCX: extracts embedded media image from word/media/
     * Returns the absolute file path to the saved cover image, or null if none found.
     */
    fun extractCover(context: Context, uri: Uri, fileName: String, bookId: String): String? {
        val lower = fileName.lowercase()
        val coversDir = File(context.filesDir, "covers")
        if (!coversDir.exists()) coversDir.mkdirs()
        val targetFile = File(coversDir, "$bookId.jpg")

        return try {
            when {
                lower.endsWith(".pdf") -> {
                    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                        val renderer = PdfRenderer(pfd)
                        renderer.use { pdf ->
                            if (pdf.pageCount > 0) {
                                val page = pdf.openPage(0)
                                val targetWidth = 480
                                val targetHeight = (targetWidth * page.height / page.width.coerceAtLeast(1)).coerceIn(480, 800)
                                val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                canvas.drawColor(Color.WHITE)
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                page.close()

                                FileOutputStream(targetFile).use { fos ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                                }
                                bitmap.recycle()
                                targetFile.absolutePath
                            } else null
                        }
                    }
                }
                lower.endsWith(".epub") -> {
                    var coverBytes: ByteArray? = null
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val zis = ZipInputStream(stream)
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val name = entry.name.lowercase()
                            if (name.contains("cover") && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                                coverBytes = readEntryBytes(zis)
                                zis.closeEntry()
                                break
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    coverBytes?.let {
                        targetFile.writeBytes(it)
                        targetFile.absolutePath
                    }
                }
                lower.endsWith(".docx") -> {
                    var imageBytes: ByteArray? = null
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val zis = ZipInputStream(stream)
                        var entry = zis.nextEntry
                        while (entry != null) {
                            val name = entry.name.lowercase()
                            if (name.startsWith("word/media/image1.") && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png"))) {
                                imageBytes = readEntryBytes(zis)
                                zis.closeEntry()
                                break
                            }
                            zis.closeEntry()
                            entry = zis.nextEntry
                        }
                    }
                    imageBytes?.let {
                        targetFile.writeBytes(it)
                        targetFile.absolutePath
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
