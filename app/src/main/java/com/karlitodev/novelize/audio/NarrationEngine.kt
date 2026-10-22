package com.karlitodev.novelize.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.ReadingProgress
import com.karlitodev.novelize.model.Sentence
import com.karlitodev.novelize.model.SpeakerRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * State representing narration playback and visual synchronization.
 */
data class NarrationState(
    val isPlaying: Boolean = false,
    val currentChapterIndex: Int = 1,
    val currentParagraphIndex: Int = 0,
    val currentSentenceIndex: Int = 0,
    val currentWordIndex: Int = 0,
    val currentSpeaker: SpeakerRole = SpeakerRole.NARRATOR,
    val playbackSpeed: Float = 1.0f,
    val selectedVoicePreset: VoicePreset = VoicePreset.FEMALE_1,
    val sleepTimerRemainingSeconds: Int? = null,
    val isTtsInitialized: Boolean = false
)

/**
 * Optimized native Android TextToSpeech narration engine.
 * Pure native Android TTS with 4 optimized voices (2 female, 2 male),
 * real-time word synchronization, background audio focus, and media controls.
 */
class NarrationEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : NarrationPlaybackService.PlaybackController, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    val voiceProfileManager = VoiceProfileManager(context)
    private var sleepTimerJob: Job? = null

    private val _state = MutableStateFlow(NarrationState())
    val state: StateFlow<NarrationState> = _state.asStateFlow()

    private var activeChapter: Chapter? = null
    private var chapterLanguage: String = "fr"
    private var selectedPreset: VoicePreset = VoicePreset.FEMALE_1

    init {
        tts = TextToSpeech(context.applicationContext, this)
        NarrationPlaybackService.playbackController = this

        voiceProfileManager.onAudioFocusLost = {
            scope.launch { pause() }
        }
        voiceProfileManager.onAudioFocusGained = {
            // Keep paused on focus regain, wait for user action
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setupUtteranceListener()
            _state.value = _state.value.copy(isTtsInitialized = true)
        } else {
            _state.value = _state.value.copy(isTtsInitialized = false)
        }
    }

    private fun setupUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = _state.value.copy(isPlaying = true)
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    handleSentenceFinished()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch {
                    _state.value = _state.value.copy(isPlaying = false)
                    updateNotification(false)
                }
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                // Approximate word index from character start offset
                val currentText = getCurrentSentence()?.text ?: ""
                val textBefore = currentText.take(start)
                val wordIdx = textBefore.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                _state.value = _state.value.copy(currentWordIndex = wordIdx)
            }
        })
    }

    fun setChapter(chapter: Chapter, initialProgress: ReadingProgress? = null, language: String = "fr") {
        activeChapter = chapter
        chapterLanguage = language

        val pIndex = initialProgress?.paragraphIndex ?: 0
        val sIndex = initialProgress?.sentenceIndex ?: 0
        val wIndex = initialProgress?.wordIndex ?: 0

        _state.value = _state.value.copy(
            currentChapterIndex = chapter.chapterIndex,
            currentParagraphIndex = pIndex.coerceIn(0, (chapter.paragraphs.size - 1).coerceAtLeast(0)),
            currentSentenceIndex = sIndex,
            currentWordIndex = wIndex,
            isPlaying = false
        )
    }

    fun setVoicePreset(preset: VoicePreset) {
        selectedPreset = preset
        _state.value = _state.value.copy(selectedVoicePreset = preset)
        if (_state.value.isPlaying) {
            speakCurrentSentence()
        }
    }

    // PlaybackController interface — notification/media button callbacks
    override fun onPlay()  { play() }
    override fun onPause() { pause() }
    override fun onNext()  { next() }
    override fun onPrev()  { previous() }
    override fun onStop()  { pause(); NarrationPlaybackService.stopService(context) }

    fun play() {
        val chapter = activeChapter ?: return
        if (chapter.paragraphs.isEmpty()) return

        if (!voiceProfileManager.requestAudioFocus()) return

        _state.value = _state.value.copy(isPlaying = true)
        updateNotification(true)
        speakCurrentSentence()
    }

    fun pause() {
        tts?.stop()
        voiceProfileManager.abandonAudioFocus()
        _state.value = _state.value.copy(isPlaying = false)
        updateNotification(false)
    }

    override fun previous() {
        val chapter = activeChapter ?: return
        var pIdx = _state.value.currentParagraphIndex
        var sIdx = _state.value.currentSentenceIndex

        if (sIdx > 0) {
            sIdx--
        } else if (pIdx > 0) {
            pIdx--
            val prevParagraph = chapter.paragraphs.getOrNull(pIdx)
            sIdx = ((prevParagraph?.sentences?.size ?: 1) - 1).coerceAtLeast(0)
        }

        _state.value = _state.value.copy(
            currentParagraphIndex = pIdx,
            currentSentenceIndex = sIdx,
            currentWordIndex = 0
        )

        if (_state.value.isPlaying) {
            speakCurrentSentence()
        }
    }

    override fun next() {
        val chapter = activeChapter ?: return
        var pIdx = _state.value.currentParagraphIndex
        var sIdx = _state.value.currentSentenceIndex
        val currentParagraph = chapter.paragraphs.getOrNull(pIdx) ?: return

        if (sIdx < currentParagraph.sentences.size - 1) {
            sIdx++
        } else if (pIdx < chapter.paragraphs.size - 1) {
            pIdx++
            sIdx = 0
        } else {
            pause()
            return
        }

        _state.value = _state.value.copy(
            currentParagraphIndex = pIdx,
            currentSentenceIndex = sIdx,
            currentWordIndex = 0
        )

        if (_state.value.isPlaying) {
            speakCurrentSentence()
        }
    }

    fun jumpToSentence(paragraphIndex: Int, sentenceIndex: Int) {
        val chapter = activeChapter ?: return
        val p = chapter.paragraphs.getOrNull(paragraphIndex) ?: return
        if (sentenceIndex !in p.sentences.indices) return

        _state.value = _state.value.copy(
            currentParagraphIndex = paragraphIndex,
            currentSentenceIndex = sentenceIndex,
            currentWordIndex = 0,
            currentSpeaker = p.speakerRole
        )

        if (_state.value.isPlaying) {
            speakCurrentSentence()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        val clamped = speed.coerceIn(0.5f, 2.5f)
        _state.value = _state.value.copy(playbackSpeed = clamped)
        if (_state.value.isPlaying) {
            speakCurrentSentence()
        }
    }

    fun togglePlayback() {
        if (_state.value.isPlaying) pause() else play()
    }

    fun previousSentence() = previous()

    fun nextSentence() = next()

    fun jumpToPosition(paragraphIndex: Int, sentenceIndex: Int) = jumpToSentence(paragraphIndex, sentenceIndex)

    fun startSleepTimer(minutes: Int) = setSleepTimer(minutes)

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerRemainingSeconds = null)
            return
        }

        val totalSeconds = minutes * 60
        _state.value = _state.value.copy(sleepTimerRemainingSeconds = totalSeconds)

        sleepTimerJob = scope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _state.value = _state.value.copy(sleepTimerRemainingSeconds = remaining)
            }
            pause()
            _state.value = _state.value.copy(sleepTimerRemainingSeconds = null)
        }
    }

    private fun speakCurrentSentence() {
        val sentence = getCurrentSentence() ?: run {
            pause()
            return
        }

        val paragraph = activeChapter?.paragraphs?.getOrNull(_state.value.currentParagraphIndex)
        val role = paragraph?.speakerRole ?: SpeakerRole.NARRATOR
        _state.value = _state.value.copy(currentSpeaker = role)

        voiceProfileManager.applySpeakerProfile(
            tts = tts,
            role = role,
            languageCode = chapterLanguage,
            baseSpeed = _state.value.playbackSpeed,
            primaryPreset = selectedPreset
        )

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        val utteranceId = UUID.randomUUID().toString()
        tts?.speak(sentence.text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun handleSentenceFinished() {
        val chapter = activeChapter ?: return
        var pIdx = _state.value.currentParagraphIndex
        var sIdx = _state.value.currentSentenceIndex
        val currentParagraph = chapter.paragraphs.getOrNull(pIdx) ?: return

        if (sIdx < currentParagraph.sentences.size - 1) {
            sIdx++
        } else if (pIdx < chapter.paragraphs.size - 1) {
            pIdx++
            sIdx = 0
        } else {
            // End of chapter reached
            pause()
            return
        }

        _state.value = _state.value.copy(
            currentParagraphIndex = pIdx,
            currentSentenceIndex = sIdx,
            currentWordIndex = 0
        )

        if (_state.value.isPlaying) {
            speakCurrentSentence()
        }
    }

    private fun getCurrentSentence(): Sentence? {
        val chapter = activeChapter ?: return null
        val p = chapter.paragraphs.getOrNull(_state.value.currentParagraphIndex) ?: return null
        return p.sentences.getOrNull(_state.value.currentSentenceIndex)
    }

    private fun updateNotification(isPlaying: Boolean) {
        val chapter = activeChapter ?: return
        NarrationPlaybackService.startService(
            context = context,
            title = chapter.title,
            subtitle = "Chapter ${chapter.chapterIndex}",
            isPlaying = isPlaying
        )
    }

    fun release() {
        sleepTimerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        voiceProfileManager.abandonAudioFocus()
        NarrationPlaybackService.stopService(context)
    }
}
