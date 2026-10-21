package com.karlitodev.novelize.audio

import android.content.Context
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
    val isTtsInitialized: Boolean = false,
    val availableSfxCount: Int = 0,
    val activeAiVoiceModelName: String? = null,
    val unsupportedLanguageNotice: String? = null
)

/**
 * Core audio drama and multi-voice narration manager.
 * Uses on-device LocalNeuralAiVoiceEngine for local neural voice synthesis.
 * Coordinates background sound effects, ambient soundscapes, sleep timers,
 * audio focus, and media playback notifications.
 */
class NarrationEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : NarrationPlaybackService.PlaybackController {

    companion object {
        const val VOICE_VOLUME_NARRATOR = 1.0f
        const val VOICE_VOLUME_CHARACTER = 0.8f
        const val SFX_BACKGROUND_DUCK = 0.35f
        const val AMBIENT_BACKGROUND_DUCK = 0.25f
    }

    val localNeuralVoiceEngine = LocalNeuralAiVoiceEngine(context, scope)
    val sfxAudioPlayer = SfxAudioPlayer(context)
    val voiceProfileManager = VoiceProfileManager(context)
    val aiVoiceEngine = AiVoiceEngine(context)
    private var sleepTimerJob: Job? = null

    private val _state = MutableStateFlow(NarrationState(availableSfxCount = sfxAudioPlayer.getDetectedCount()))
    val state: StateFlow<NarrationState> = _state.asStateFlow()

    private var activeChapter: Chapter? = null
    private var chapterLanguage: String = "fr"

    init {
        // Initialize on-device neural voice engine
        localNeuralVoiceEngine.initialize()
        localNeuralVoiceEngine.onWordProgress = { wordIndex ->
            _state.value = _state.value.copy(currentWordIndex = wordIndex)
        }
        localNeuralVoiceEngine.onSentenceComplete = {
            scope.launch {
                handleSentenceFinished()
            }
        }
        localNeuralVoiceEngine.onError = {
            scope.launch {
                _state.value = _state.value.copy(isPlaying = false)
                sfxAudioPlayer.releaseDucking()
                sfxAudioPlayer.pauseAmbient()
                updateNotification(false)
            }
        }

        _state.value = _state.value.copy(
            isTtsInitialized = true,
            availableSfxCount = sfxAudioPlayer.getDetectedCount()
        )

        // Setup notification and headset media controller listener
        NarrationPlaybackService.playbackController = this

        // Audio focus listeners
        voiceProfileManager.onAudioFocusLost = {
            scope.launch { pause() }
        }
        voiceProfileManager.onAudioFocusGained = {
            // Audio focus recovered
        }
    }

    fun setChapter(chapter: Chapter, initialProgress: ReadingProgress? = null, language: String = "fr") {
        activeChapter = chapter
        chapterLanguage = language

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
        if (!voiceProfileManager.requestAudioFocus()) {
            return
        }

        _state.value = _state.value.copy(isPlaying = true)
        sfxAudioPlayer.resumeAmbient()
        speakCurrentSentence(chapter)
        updateNotification(true)
    }

    fun pause() {
        localNeuralVoiceEngine.pause()
        sfxAudioPlayer.releaseDucking()
        sfxAudioPlayer.pauseAmbient()
        voiceProfileManager.abandonAudioFocus()
        _state.value = _state.value.copy(isPlaying = false)
        updateNotification(false)
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
        localNeuralVoiceEngine.setPlaybackSpeed(speed)
    }

    fun setSfxVolume(volume: Float) {
        sfxAudioPlayer.sfxVolume = volume
    }

    fun setAmbientVolume(volume: Float) {
        sfxAudioPlayer.ambientVolume = volume
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

    fun refreshAudioCues() {
        sfxAudioPlayer.reloadAudioResources()
        _state.value = _state.value.copy(availableSfxCount = sfxAudioPlayer.getDetectedCount())
    }

    private fun speakCurrentSentence(chapter: Chapter) {
        val pIndex = _state.value.currentParagraphIndex
        val sIndex = _state.value.currentSentenceIndex

        val paragraph = chapter.paragraphs.getOrNull(pIndex) ?: run {
            _state.value = _state.value.copy(isPlaying = false)
            sfxAudioPlayer.stopAmbient()
            updateNotification(false)
            return
        }

        val sentence = paragraph.sentences.getOrNull(sIndex) ?: run {
            _state.value = _state.value.copy(isPlaying = false)
            sfxAudioPlayer.stopAmbient()
            updateNotification(false)
            return
        }

        // Verify that the text language is supported
        val eligibility = aiVoiceEngine.checkEligibility(chapterLanguage, sentence.text)
        if (eligibility is VoiceSynthesisEligibility.UnsupportedLanguage) {
            _state.value = _state.value.copy(
                isPlaying = false,
                unsupportedLanguageNotice = "Language unsupported: only English and French texts are supported by neural AI models."
            )
            sfxAudioPlayer.stopAmbient()
            voiceProfileManager.abandonAudioFocus()
            updateNotification(false)
            return
        }

        // Resolve Neural AI voice model for speaker role
        val neuralModel = aiVoiceEngine.getModelForRole(paragraph.speakerRole, chapterLanguage)

        // Duck SFX and ambient loops to background sound levels during narration
        sfxAudioPlayer.applyBackgroundDucking(
            sfxDuck = SFX_BACKGROUND_DUCK,
            ambientDuck = AMBIENT_BACKGROUND_DUCK
        )

        _state.value = _state.value.copy(
            currentSpeaker = paragraph.speakerRole,
            activeAiVoiceModelName = neuralModel.name,
            unsupportedLanguageNotice = null
        )

        // Trigger contextual background sound effects and ambient soundscapes
        triggerAudioCues(sentence)

        // Synthesize and play with on-device neural voice engine
        localNeuralVoiceEngine.speakSentence(sentence.text, paragraph.speakerRole, chapterLanguage)

        updateNotification(true, sentence.text)
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
            sfxAudioPlayer.releaseDucking()
            sfxAudioPlayer.stopAmbient()
            voiceProfileManager.abandonAudioFocus()
            updateNotification(false)
        }
    }

    private fun triggerAudioCues(sentence: Sentence) {
        val sfxId = sentence.sfxCue
        if (sfxId != null) {
            sfxAudioPlayer.playSfx(sfxId)
            val sfxItem = SfxCatalog.items.firstOrNull { it.id == sfxId }
            _state.value = _state.value.copy(activeSfxTitle = sfxItem?.title)
        } else {
            _state.value = _state.value.copy(activeSfxTitle = null)
        }

        val ambientId = sentence.ambientCue
        if (ambientId != null) {
            val ambientItem = SfxCatalog.items.firstOrNull { it.id == ambientId }
            if (ambientItem != null) {
                sfxAudioPlayer.playAmbient(ambientItem)
                _state.value = _state.value.copy(activeAmbientTitle = ambientItem.title)
            }
        }
    }

    private fun updateNotification(isPlaying: Boolean, subtitle: String? = null) {
        val title = activeChapter?.title ?: "NoveLize Reader"
        val sub = subtitle ?: if (isPlaying) "Playing Neural Audio Drama" else "Narration Paused"
        NarrationPlaybackService.startService(context, title, sub, isPlaying)
    }

    fun release() {
        localNeuralVoiceEngine.release()
        sfxAudioPlayer.release()
        voiceProfileManager.abandonAudioFocus()
    }

    // PlaybackController callbacks
    override fun onPlay() {
        play()
    }

    override fun onPause() {
        pause()
    }

    override fun onNext() {
        nextSentence()
    }

    override fun onPrev() {
        previousSentence()
    }

    override fun onStop() {
        pause()
    }
}
