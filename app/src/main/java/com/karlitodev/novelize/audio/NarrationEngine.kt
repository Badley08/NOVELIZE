package com.karlitodev.novelize.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.karlitodev.novelize.model.Chapter
import com.karlitodev.novelize.model.ReadingProgress
import com.karlitodev.novelize.model.Sentence
import com.karlitodev.novelize.model.SpeakerRole
import com.karlitodev.novelize.sfx.SfxCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

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
    val activeSfxTitle: String? = null,
    val activeAmbientTitle: String? = null,
    val sleepTimerRemainingSeconds: Int? = null,
    val isTtsInitialized: Boolean = false
)

/**
 * Core audio drama and multi-voice narration manager.
 * Orchestrates multi-voice TTS, sound effects triggers, sleep timers, and karaoke highlighting.
 */
class NarrationEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var soundPool: SoundPool? = null
    private var ambientPlayer: MediaPlayer? = null
    private var sleepTimerJob: Job? = null

    private val _state = MutableStateFlow(NarrationState())
    val state: StateFlow<NarrationState> = _state.asStateFlow()

    private var activeChapter: Chapter? = null

    private val soundMap = mutableMapOf<String, Int>()

    init {
        tts = TextToSpeech(context.applicationContext, this)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        try {
            val clashRes = context.resources.getIdentifier("sword_clash", "raw", context.packageName)
            if (clashRes != 0) {
                soundPool?.load(context, clashRes, 1)?.let { soundMap["sword_clash"] = it }
            }
            val slashRes = context.resources.getIdentifier("sword_slash", "raw", context.packageName)
            if (slashRes != 0) {
                soundPool?.load(context, slashRes, 1)?.let { soundMap["sword_slash"] = it }
            }
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // Utterance started
                }

                override fun onDone(utteranceId: String?) {
                    scope.launch {
                        handleSentenceFinished()
                    }
                }

                override fun onError(utteranceId: String?) {
                    scope.launch {
                        _state.value = _state.value.copy(isPlaying = false)
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    // Word-level progress for smart resume
                    _state.value = _state.value.copy(currentWordIndex = start)
                }
            })
            _state.value = _state.value.copy(isTtsInitialized = true)
        }
    }

    fun setChapter(chapter: Chapter, initialProgress: ReadingProgress? = null) {
        activeChapter = chapter
        if (initialProgress != null) {
            _state.value = _state.value.copy(
                currentChapterIndex = initialProgress.chapterIndex,
                currentParagraphIndex = initialProgress.paragraphIndex,
                currentSentenceIndex = initialProgress.sentenceIndex,
                currentWordIndex = initialProgress.wordIndex
            )
        } else {
            _state.value = _state.value.copy(
                currentChapterIndex = chapter.chapterIndex,
                currentParagraphIndex = 0,
                currentSentenceIndex = 0,
                currentWordIndex = 0
            )
        }
    }

    fun play() {
        val chapter = activeChapter ?: return
        _state.value = _state.value.copy(isPlaying = true)
        speakCurrentSentence(chapter)
    }

    fun pause() {
        tts?.stop()
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun togglePlayback() {
        if (_state.value.isPlaying) pause() else play()
    }

    fun jumpToPosition(paragraphIndex: Int, sentenceIndex: Int, wordIndex: Int = 0) {
        val wasPlaying = _state.value.isPlaying
        pause()
        _state.value = _state.value.copy(
            currentParagraphIndex = paragraphIndex,
            currentSentenceIndex = sentenceIndex,
            currentWordIndex = wordIndex
        )
        if (wasPlaying) {
            play()
        }
    }

    fun nextSentence() {
        val chapter = activeChapter ?: return
        val currentP = _state.value.currentParagraphIndex
        val currentS = _state.value.currentSentenceIndex

        val paragraph = chapter.paragraphs.getOrNull(currentP) ?: return
        if (currentS + 1 < paragraph.sentences.size) {
            jumpToPosition(currentP, currentS + 1)
        } else if (currentP + 1 < chapter.paragraphs.size) {
            jumpToPosition(currentP + 1, 0)
        }
    }

    fun previousSentence() {
        val chapter = activeChapter ?: return
        val currentP = _state.value.currentParagraphIndex
        val currentS = _state.value.currentSentenceIndex

        if (currentS > 0) {
            jumpToPosition(currentP, currentS - 1)
        } else if (currentP > 0) {
            val prevParagraph = chapter.paragraphs[currentP - 1]
            jumpToPosition(currentP - 1, maxOf(0, prevParagraph.sentences.size - 1))
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
        tts?.setSpeechRate(speed)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
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

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _state.value = _state.value.copy(sleepTimerRemainingSeconds = null)
    }

    private fun speakCurrentSentence(chapter: Chapter) {
        val pIndex = _state.value.currentParagraphIndex
        val sIndex = _state.value.currentSentenceIndex

        val paragraph = chapter.paragraphs.getOrNull(pIndex) ?: run {
            _state.value = _state.value.copy(isPlaying = false)
            return
        }

        val sentence = paragraph.sentences.getOrNull(sIndex) ?: run {
            _state.value = _state.value.copy(isPlaying = false)
            return
        }

        // Apply distinct multi-voice profile based on speaker role
        applyVoiceProfile(paragraph.speakerRole)
        _state.value = _state.value.copy(currentSpeaker = paragraph.speakerRole)

        // Trigger contextual sound effects if present
        triggerSfxIfPresent(sentence)

        val params = Bundle()
        val utteranceId = "utterance_${pIndex}_${sIndex}"
        tts?.speak(sentence.text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun handleSentenceFinished() {
        val chapter = activeChapter ?: return
        if (!_state.value.isPlaying) return

        val pIndex = _state.value.currentParagraphIndex
        val sIndex = _state.value.currentSentenceIndex

        val currentParagraph = chapter.paragraphs.getOrNull(pIndex) ?: return

        if (sIndex + 1 < currentParagraph.sentences.size) {
            _state.value = _state.value.copy(
                currentSentenceIndex = sIndex + 1,
                currentWordIndex = 0
            )
            speakCurrentSentence(chapter)
        } else if (pIndex + 1 < chapter.paragraphs.size) {
            _state.value = _state.value.copy(
                currentParagraphIndex = pIndex + 1,
                currentSentenceIndex = 0,
                currentWordIndex = 0
            )
            speakCurrentSentence(chapter)
        } else {
            // End of chapter reached
            _state.value = _state.value.copy(isPlaying = false)
        }
    }

    private fun applyVoiceProfile(speaker: SpeakerRole) {
        when (speaker) {
            SpeakerRole.NARRATOR -> {
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(_state.value.playbackSpeed)
            }
            SpeakerRole.HERO -> {
                tts?.setPitch(0.9f)
                tts?.setSpeechRate(_state.value.playbackSpeed * 1.05f)
            }
            SpeakerRole.FEMALE_1 -> {
                tts?.setPitch(1.3f)
                tts?.setSpeechRate(_state.value.playbackSpeed * 1.0f)
            }
            SpeakerRole.MALE_1 -> {
                tts?.setPitch(0.8f)
                tts?.setSpeechRate(_state.value.playbackSpeed * 0.95f)
            }
            SpeakerRole.SUPPORTING -> {
                tts?.setPitch(1.15f)
                tts?.setSpeechRate(_state.value.playbackSpeed)
            }
        }
    }

    private fun triggerSfxIfPresent(sentence: Sentence) {
        val sfxItem = sentence.sfxCue?.let { id -> SfxCatalog.items.firstOrNull { it.id == id } }
        val ambientItem = sentence.ambientCue?.let { id -> SfxCatalog.items.firstOrNull { it.id == id } }

        sentence.sfxCue?.let { cueId ->
            val soundId = soundMap[cueId]
            if (soundId != null) {
                soundPool?.play(soundId, 0.85f, 0.85f, 1, 0, 1.0f)
            }
        }

        _state.value = _state.value.copy(
            activeSfxTitle = sfxItem?.title,
            activeAmbientTitle = ambientItem?.title
        )
    }

    fun release() {
        sleepTimerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        soundPool?.release()
        ambientPlayer?.release()
    }
}
