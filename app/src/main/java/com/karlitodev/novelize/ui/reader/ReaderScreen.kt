package com.karlitodev.novelize.ui.reader

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.karlitodev.novelize.audio.NarrationState
import com.karlitodev.novelize.data.ReaderTheme
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.Paragraph
import com.karlitodev.novelize.model.QuizQuestion
import com.karlitodev.novelize.model.SpeakerRole
import com.karlitodev.novelize.quiz.QuizEngine
import com.karlitodev.novelize.theme.*
import com.karlitodev.novelize.translation.DefaultTranslationService
import com.karlitodev.novelize.translation.TranslationResult
import kotlinx.coroutines.launch

/**
 * Immersive reader screen with synchronized karaoke highlighting,
 * contextual sound design cues, multi-voice controls, and long-press actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapter: Chapter,
    narrationState: NarrationState,
    theme: ReaderTheme,
    fontSizeSp: Float,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onPreviousSentence: () -> Unit,
    onNextSentence: () -> Unit,
    onSentenceClick: (Int, Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSleepTimerSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // State for contextual word action dialog
    var selectedWordForAction by remember { mutableStateOf<String?>(null) }
    var translationDialogResult by remember { mutableStateOf<TranslationResult?>(null) }
    var isTranslating by remember { mutableStateOf(false) }

    // State for manual stop marker (smart resume)
    var manualStopPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // State for highlighted sentences/passages
    val userHighlights = remember { mutableStateListOf<Pair<Int, Int>>() }
    val userUnderlines = remember { mutableStateListOf<Pair<Int, Int>>() }

    // State for end-of-chapter quiz
    var showQuizDialog by remember { mutableStateOf(false) }
    var quizQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }

    // State for speed menu & sleep timer dialog
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showSleepTimerMenu by remember { mutableStateOf(false) }

    val translationService = remember { DefaultTranslationService() }

    // Theme color resolution
    val (backgroundColor, textColor, karaokeHighlightColor, surfaceBarColor) = when (theme) {
        ReaderTheme.LIGHT -> Quad(LightBackground, LightOnBackground, LightKaraokeHighlight, LightSurface)
        ReaderTheme.SEPIA -> Quad(SepiaBackground, SepiaOnBackground, SepiaKaraokeHighlight, SepiaSurface)
        ReaderTheme.NIGHT -> Quad(NightBackground, NightOnBackground, NightKaraokeHighlight, NightSurface)
        ReaderTheme.DYNAMIC -> Quad(Color(0xFF13111C), Color(0xFFEDE9FE), Color(0x556C5CE7), Color(0xFF1E1B2E))
    }

    // Auto-scroll to active sentence when narration advances
    LaunchedEffect(narrationState.currentParagraphIndex) {
        if (narrationState.isPlaying) {
            coroutineScope.launch {
                listState.animateScrollToItem(narrationState.currentParagraphIndex)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapter.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1
                        )
                        narrationState.activeAmbientTitle?.let { ambient ->
                            Text(
                                text = "🌧️ $ambient",
                                fontSize = 11.sp,
                                color = BrandTeal
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // "Back to where I stopped" quick jump button
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(narrationState.currentParagraphIndex)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Back to where I stopped",
                            tint = BrandPurpleLight
                        )
                    }

                    // Chapter Quiz button
                    IconButton(
                        onClick = {
                            quizQuestions = QuizEngine.generateQuizForChapter(chapter)
                            showQuizDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = "Chapter Quiz",
                            tint = BrandAmber
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceBarColor)
            )
        },
        bottomBar = {
            // Audio Drama player controls
            PlayerBar(
                narrationState = narrationState,
                surfaceColor = surfaceBarColor,
                textColor = textColor,
                onPlayPause = onPlayPause,
                onPrevious = onPreviousSentence,
                onNext = onNextSentence,
                onSpeedClick = { showSpeedMenu = true },
                onTimerClick = { showSleepTimerMenu = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(chapter.paragraphs) { pIndex, paragraph ->
                    ParagraphView(
                        paragraph = paragraph,
                        pIndex = pIndex,
                        activeParagraphIndex = narrationState.currentParagraphIndex,
                        activeSentenceIndex = narrationState.currentSentenceIndex,
                        isPlaying = narrationState.isPlaying,
                        fontSizeSp = fontSizeSp,
                        textColor = textColor,
                        karaokeColor = karaokeHighlightColor,
                        isMarkedStop = manualStopPosition == Pair(pIndex, 0),
                        isUserHighlighted = userHighlights.contains(Pair(pIndex, 0)),
                        isUserUnderlined = userUnderlines.contains(Pair(pIndex, 0)),
                        onSentenceClick = { sIndex -> onSentenceClick(pIndex, sIndex) },
                        onWordLongPress = { word -> selectedWordForAction = word }
                    )
                }

                // End of chapter comprehension quiz prompt card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, bottom = 48.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceBarColor
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "End of ${chapter.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = textColor
                            )
                            Text(
                                text = "Check your comprehension with a quick 3-question review.",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Button(
                                onClick = {
                                    quizQuestions = QuizEngine.generateQuizForChapter(chapter)
                                    showQuizDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Take Chapter Quiz")
                            }
                        }
                    }
                }
            }

            // Floating SFX Notification Pill
            narrationState.activeSfxTitle?.let { sfxTitle ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCoral)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚔️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sfxTitle,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Floating "Back to where I stopped" quick resume button
            val isScrolledAway = listState.firstVisibleItemIndex != narrationState.currentParagraphIndex
            if (isScrolledAway) {
                ExtendedFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(narrationState.currentParagraphIndex)
                        }
                    },
                    containerColor = BrandPurple,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to narration (¶${narrationState.currentParagraphIndex + 1})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Long-press Contextual Actions Bottom Sheet
    selectedWordForAction?.let { word ->
        AlertDialog(
            onDismissRequest = { selectedWordForAction = null },
            title = {
                Text(text = "“$word”", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            manualStopPosition = Pair(narrationState.currentParagraphIndex, 0)
                            selectedWordForAction = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = BrandPurple)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Mark as where I stopped", color = textColor)
                    }

                    TextButton(
                        onClick = {
                            isTranslating = true
                            coroutineScope.launch {
                                translationDialogResult = translationService.translate(word)
                                isTranslating = false
                                selectedWordForAction = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = BrandTeal)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Translate (Offline)", color = textColor)
                    }

                    TextButton(
                        onClick = {
                            val pos = Pair(narrationState.currentParagraphIndex, 0)
                            if (userHighlights.contains(pos)) userHighlights.remove(pos) else userHighlights.add(pos)
                            selectedWordForAction = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.BorderColor, contentDescription = null, tint = BrandAmber)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Highlight Color", color = textColor)
                    }

                    TextButton(
                        onClick = {
                            val pos = Pair(narrationState.currentParagraphIndex, 0)
                            if (userUnderlines.contains(pos)) userUnderlines.remove(pos) else userUnderlines.add(pos)
                            selectedWordForAction = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FormatUnderlined, contentDescription = null, tint = BrandPurpleLight)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Underline", color = textColor)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedWordForAction = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Offline Translation Result Dialog
    translationDialogResult?.let { result ->
        AlertDialog(
            onDismissRequest = { translationDialogResult = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = BrandTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Translation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = result.originalText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.translatedText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Source: On-device Lexicon (${result.sourceLanguage.uppercase()} -> ${result.targetLanguage.uppercase()})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { translationDialogResult = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Speed Selector Dialog
    if (showSpeedMenu) {
        AlertDialog(
            onDismissRequest = { showSpeedMenu = false },
            title = { Text("Narration Speed") },
            text = {
                Column {
                    listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSpeedChange(speed)
                                    showSpeedMenu = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = narrationState.playbackSpeed == speed,
                                onClick = {
                                    onSpeedChange(speed)
                                    showSpeedMenu = false
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("${speed}x", fontWeight = if (narrationState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedMenu = false }) { Text("Close") }
            }
        )
    }

    // Sleep Timer Selector Dialog
    if (showSleepTimerMenu) {
        AlertDialog(
            onDismissRequest = { showSleepTimerMenu = false },
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSleepTimerSelect(mins)
                                    showSleepTimerMenu = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = BrandPurple)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("$mins Minutes")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerMenu = false }) { Text("Cancel") }
            }
        )
    }

    // Comprehension Quiz Modal Dialog
    if (showQuizDialog && quizQuestions.isNotEmpty()) {
        QuizDialog(
            questions = quizQuestions,
            onDismiss = { showQuizDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParagraphView(
    paragraph: Paragraph,
    pIndex: Int,
    activeParagraphIndex: Int,
    activeSentenceIndex: Int,
    isPlaying: Boolean,
    fontSizeSp: Float,
    textColor: Color,
    karaokeColor: Color,
    isMarkedStop: Boolean,
    isUserHighlighted: Boolean,
    isUserUnderlined: Boolean,
    onSentenceClick: (Int) -> Unit,
    onWordLongPress: (String) -> Unit
) {
    val isDialogue = paragraph.speakerRole != SpeakerRole.NARRATOR

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isUserHighlighted) BrandAmber.copy(alpha = 0.25f)
                else Color.Transparent
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        // Speaker label for dialogue lines
        if (isDialogue) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = when (paragraph.speakerRole) {
                        SpeakerRole.HERO -> "🗡️ ${paragraph.speakerRole.displayName}"
                        SpeakerRole.FEMALE_1 -> "✨ ${paragraph.speakerRole.displayName}"
                        SpeakerRole.MALE_1 -> "🛡️ ${paragraph.speakerRole.displayName}"
                        SpeakerRole.SUPPORTING -> "📜 ${paragraph.speakerRole.displayName}"
                        else -> paragraph.speakerRole.displayName
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandPurpleLight
                )
                if (isMarkedStop) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📍 Stopped Here",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCoral
                    )
                }
            }
        }

        // Render sentences with karaoke highlight support and word-level interaction
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            paragraph.sentences.forEachIndexed { sIndex, sentence ->
                val isSentenceActive = (pIndex == activeParagraphIndex && sIndex == activeSentenceIndex && isPlaying)

                Surface(
                    color = if (isSentenceActive) karaokeColor else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    FlowRow(modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)) {
                        val words = sentence.text.split(Regex("\\s+"))
                        words.forEach { word ->
                            val cleanWord = word.trim()
                            if (cleanWord.isNotEmpty()) {
                                Text(
                                    text = "$cleanWord ",
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.55f).sp,
                                    fontWeight = if (isSentenceActive) FontWeight.SemiBold else FontWeight.Normal,
                                    fontStyle = if (isDialogue) FontStyle.Italic else FontStyle.Normal,
                                    color = if (isSentenceActive) BrandPurple else textColor,
                                    textDecoration = if (isUserUnderlined) TextDecoration.Underline else TextDecoration.None,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .combinedClickable(
                                            onClick = { onSentenceClick(sIndex) },
                                            onLongClick = { onWordLongPress(cleanWord) }
                                        )
                                        .padding(horizontal = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerBar(
    narrationState: NarrationState,
    surfaceColor: Color,
    textColor: Color,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSpeedClick: () -> Unit,
    onTimerClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = surfaceColor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Speaker tag & sleep timer indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker badge
                Surface(
                    color = BrandPurple.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Voice: ${narrationState.currentSpeaker.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Sleep timer countdown if active
                narrationState.sleepTimerRemainingSeconds?.let { remaining ->
                    Text(
                        text = "🌙 ${remaining / 60}m ${remaining % 60}s",
                        fontSize = 12.sp,
                        color = BrandAmber,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed control
                TextButton(onClick = onSpeedClick) {
                    Text(
                        text = "${narrationState.playbackSpeed}x",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                // Previous sentence
                IconButton(onClick = onPrevious) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Sentence",
                        tint = textColor
                    )
                }

                // Play / Pause FAB
                FilledIconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandPurple)
                ) {
                    Icon(
                        imageVector = if (narrationState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (narrationState.isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Next sentence
                IconButton(onClick = onNext) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Sentence",
                        tint = textColor
                    )
                }

                // Sleep timer button
                IconButton(onClick = onTimerClick) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizDialog(
    questions: List<QuizQuestion>,
    onDismiss: () -> Unit
) {
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isSubmitted by remember { mutableStateOf(false) }

    val q = questions.getOrNull(currentQuestionIndex)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (q != null) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = q.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    q.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = selectedAnswerIndex == optIndex
                        val isCorrect = optIndex == q.correctOptionIndex

                        val borderCol = when {
                            isSubmitted && isCorrect -> BrandEmerald
                            isSubmitted && isSelected && !isCorrect -> BrandCoral
                            isSelected -> BrandPurple
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable(enabled = !isSubmitted) { selectedAnswerIndex = optIndex }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = optionText,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    if (isSubmitted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = q.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!isSubmitted) {
                            Button(
                                onClick = {
                                    if (selectedAnswerIndex != null) {
                                        if (selectedAnswerIndex == q.correctOptionIndex) {
                                            score++
                                        }
                                        isSubmitted = true
                                    }
                                },
                                enabled = selectedAnswerIndex != null,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                            ) {
                                Text("Check Answer")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (currentQuestionIndex + 1 < questions.size) {
                                        currentQuestionIndex++
                                        selectedAnswerIndex = null
                                        isSubmitted = false
                                    } else {
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                            ) {
                                Text(if (currentQuestionIndex + 1 < questions.size) "Next Question" else "Finish ($score/${questions.size})")
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
