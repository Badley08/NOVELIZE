package com.karlitodev.novelize.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.karlitodev.novelize.model.SpeakerRole
import java.util.Locale

/**
 * Manages multi-voice speaker assignments, language detection, and audio focus.
 */
class VoiceProfileManager(
    private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var focusRequest: AudioFocusRequest? = null
    var onAudioFocusLost: (() -> Unit)? = null
    var onAudioFocusGained: (() -> Unit)? = null

    /**
     * Resolves the TTS locale based on book/chapter language code.
     */
    fun resolveLocale(languageCode: String): Locale {
        return when (languageCode.lowercase().take(2)) {
            "fr" -> Locale.FRENCH
            "en" -> Locale.ENGLISH
            "ja" -> Locale.JAPANESE
            "es" -> Locale("es", "ES")
            "de" -> Locale.GERMAN
            else -> Locale.getDefault()
        }
    }

    /**
     * Configures the TTS engine for the specified speaker role and language.
     */
    fun applySpeakerProfile(
        tts: TextToSpeech?,
        role: SpeakerRole,
        languageCode: String,
        baseSpeed: Float
    ) {
        if (tts == null) return

        val locale = resolveLocale(languageCode)
        try {
            tts.language = locale
        } catch (_: Exception) {}

        // Try to match distinct system voices for roles if available
        val matchedVoice = findMatchingVoice(tts, role, locale)
        if (matchedVoice != null) {
            try {
                tts.voice = matchedVoice
            } catch (_: Exception) {}
        }

        // Apply role-specific pitch and speech rate modulation
        when (role) {
            SpeakerRole.NARRATOR -> {
                tts.setPitch(1.0f)
                tts.setSpeechRate(baseSpeed)
            }
            SpeakerRole.HERO -> {
                tts.setPitch(0.92f)
                tts.setSpeechRate(baseSpeed * 1.05f)
            }
            SpeakerRole.FEMALE_1 -> {
                tts.setPitch(1.30f)
                tts.setSpeechRate(baseSpeed * 1.0f)
            }
            SpeakerRole.MALE_1 -> {
                tts.setPitch(0.78f)
                tts.setSpeechRate(baseSpeed * 0.95f)
            }
            SpeakerRole.SUPPORTING -> {
                tts.setPitch(1.15f)
                tts.setSpeechRate(baseSpeed * 1.02f)
            }
        }
    }

    /**
     * Searches installed system voices matching role gender and locale.
     */
    private fun findMatchingVoice(tts: TextToSpeech, role: SpeakerRole, locale: Locale): Voice? {
        val availableVoices = try { tts.voices } catch (_: Exception) { null } ?: return null
        val localeVoices = availableVoices.filter { it.locale.language == locale.language }
        if (localeVoices.isEmpty()) return null

        return when (role) {
            SpeakerRole.FEMALE_1 -> {
                localeVoices.firstOrNull { it.name.contains("female", ignoreCase = true) || it.name.contains("femme", ignoreCase = true) }
            }
            SpeakerRole.MALE_1, SpeakerRole.HERO -> {
                localeVoices.firstOrNull { it.name.contains("male", ignoreCase = true) || it.name.contains("homme", ignoreCase = true) }
            }
            else -> localeVoices.firstOrNull()
        }
    }

    /**
     * Requests media audio focus before starting narration.
     */
    fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            onAudioFocusLost?.invoke()
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            onAudioFocusGained?.invoke()
                        }
                    }
                }
                .build()

            focusRequest = request
            val result = am.requestAudioFocus(request)
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            val result = am.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                        onAudioFocusLost?.invoke()
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        onAudioFocusGained?.invoke()
                    }
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    /**
     * Abandons audio focus when narration stops.
     */
    fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
    }
}
