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
 * The 4 optimized TTS voice presets:
 * 2 Female voices + 2 Male voices.
 */
enum class VoicePreset(
    val id: String,
    val displayName: String,
    val gender: String, // "F" or "M"
    val basePitch: Float,
    val rateModifier: Float,
    val description: String
) {
    FEMALE_1("female_1", "Aria", "F", 1.15f, 1.0f, "Voix féminine douce et mélodieuse"),
    FEMALE_2("female_2", "Elena", "F", 1.32f, 1.04f, "Voix féminine vive et expressive"),
    MALE_1("male_1", "Marcus", "M", 0.84f, 0.98f, "Voix masculine calme et posée"),
    MALE_2("male_2", "Julian", "M", 0.70f, 0.94f, "Voix masculine grave et profonde");

    companion object {
        fun fromId(id: String): VoicePreset {
            return entries.firstOrNull { it.id == id } ?: FEMALE_1
        }
    }
}

/**
 * Manages native Android TextToSpeech configuration, audio focus,
 * and the 4 dedicated voice profiles.
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
     * Configures the TTS engine for the specified voice preset and speed.
     */
    fun applyVoicePreset(
        tts: TextToSpeech?,
        preset: VoicePreset,
        languageCode: String,
        baseSpeed: Float
    ) {
        if (tts == null) return
        val locale = resolveLocale(languageCode)
        try {
            tts.language = locale
        } catch (_: Exception) {}

        // Match system voice matching gender if installed
        val matchedVoice = findMatchingVoiceByGender(tts, preset.gender, locale)
        if (matchedVoice != null) {
            try {
                tts.voice = matchedVoice
            } catch (_: Exception) {}
        }

        // Apply profile-specific pitch and speech rate modulation
        tts.setPitch(preset.basePitch)
        tts.setSpeechRate(baseSpeed * preset.rateModifier)
    }

    /**
     * Maps speaker roles in novel dialogues to the 4 voice presets.
     */
    fun applySpeakerProfile(
        tts: TextToSpeech?,
        role: SpeakerRole,
        languageCode: String,
        baseSpeed: Float,
        primaryPreset: VoicePreset = VoicePreset.FEMALE_1
    ) {
        val preset = when (role) {
            SpeakerRole.NARRATOR -> primaryPreset
            SpeakerRole.HERO -> if (primaryPreset.gender == "M") VoicePreset.MALE_1 else VoicePreset.FEMALE_1
            SpeakerRole.FEMALE_1 -> VoicePreset.FEMALE_2
            SpeakerRole.MALE_1 -> VoicePreset.MALE_2
            SpeakerRole.SUPPORTING -> if (primaryPreset.gender == "F") VoicePreset.MALE_1 else VoicePreset.FEMALE_2
        }
        applyVoicePreset(tts, preset, languageCode, baseSpeed)
    }

    /**
     * Searches installed system voices matching gender and locale.
     */
    private fun findMatchingVoiceByGender(tts: TextToSpeech, gender: String, locale: Locale): Voice? {
        val availableVoices = try { tts.voices } catch (_: Exception) { null } ?: return null
        val localeVoices = availableVoices.filter { it.locale.language == locale.language }
        if (localeVoices.isEmpty()) return null

        val isFemale = gender == "F"
        return if (isFemale) {
            localeVoices.firstOrNull { it.name.contains("female", ignoreCase = true) || it.name.contains("femme", ignoreCase = true) }
                ?: localeVoices.firstOrNull()
        } else {
            localeVoices.firstOrNull { it.name.contains("male", ignoreCase = true) || it.name.contains("homme", ignoreCase = true) }
                ?: localeVoices.lastOrNull()
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
