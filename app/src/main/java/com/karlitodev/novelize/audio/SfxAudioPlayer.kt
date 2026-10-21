package com.karlitodev.novelize.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import com.karlitodev.novelize.sfx.SfxCatalog
import com.karlitodev.novelize.sfx.SfxItem
import java.io.File

/**
 * Manages playback of sound effects (SoundPool) and ambient loops (MediaPlayer).
 * Scans app-specific storage and embedded raw resources for the 50 SFX checklist files.
 */
class SfxAudioPlayer(
    private val context: Context
) {
    private val soundPool: SoundPool
    private val loadedSoundIds = mutableMapOf<String, Int>()
    private var ambientPlayer: MediaPlayer? = null
    private var currentAmbientId: String? = null

    /** Base SFX volume (user preference, 0.0–1.0) */
    var sfxVolume: Float = 0.8f

    /** Base ambient volume (user preference, 0.0–1.0) */
    var ambientVolume: Float = 0.5f
        set(value) {
            field = value
            ambientPlayer?.setVolume(value * ambientDuckMultiplier, value * ambientDuckMultiplier)
        }

    /**
     * Background sound attenuation ratio for sound effects.
     * Keeps sound effects at a subtle level so narration remains distinct.
     */
    private val backgroundSoundRatio: Float = 0.35f

    /**
     * Ducking multiplier applied to SFX one-shots while voice is speaking.
     */
    private var sfxDuckMultiplier: Float = 0.5f

    /**
     * Ducking multiplier applied to ambient loops while voice is speaking.
     */
    private var ambientDuckMultiplier: Float = 0.35f

    /**
     * Activates muted background sound mode during voice narration.
     */
    fun applyBackgroundDucking(sfxDuck: Float = 0.4f, ambientDuck: Float = 0.3f) {
        sfxDuckMultiplier = sfxDuck.coerceIn(0f, 1f)
        ambientDuckMultiplier = ambientDuck.coerceIn(0f, 1f)
        try {
            val duckedAmbientVol = ambientVolume * ambientDuckMultiplier
            ambientPlayer?.setVolume(duckedAmbientVol, duckedAmbientVol)
        } catch (_: Exception) {}
    }

    /**
     * Restores background sound level after sentence completion.
     */
    fun releaseDucking() {
        sfxDuckMultiplier = backgroundSoundRatio
        ambientDuckMultiplier = backgroundSoundRatio
        try {
            val vol = ambientVolume * backgroundSoundRatio
            ambientPlayer?.setVolume(vol, vol)
        } catch (_: Exception) {}
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(audioAttributes)
            .build()

        reloadAudioResources()
    }

    /**
     * Primary external directory where user drops Pixabay sound effects.
     */
    fun getSfxDirectory(): File {
        val dir = File(context.getExternalFilesDir(null), "sfx")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Internal storage fallback directory for sound effects.
     */
    fun getInternalSfxDirectory(): File {
        val dir = File(context.filesDir, "sfx")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Reloads all 50 catalog audio files from storage and raw resources.
     */
    fun reloadAudioResources() {
        val extDir = getSfxDirectory()
        val intDir = getInternalSfxDirectory()

        for (item in SfxCatalog.items) {
            if (item.isLoop) continue // Ambient loops are loaded on-demand by MediaPlayer

            // 1. Check external files dir
            val extFile = File(extDir, item.fileName)
            if (extFile.exists() && extFile.length() > 0) {
                val soundId = soundPool.load(extFile.absolutePath, 1)
                loadedSoundIds[item.id] = soundId
                continue
            }

            // 2. Check internal files dir
            val intFile = File(intDir, item.fileName)
            if (intFile.exists() && intFile.length() > 0) {
                val soundId = soundPool.load(intFile.absolutePath, 1)
                loadedSoundIds[item.id] = soundId
                continue
            }

            // 3. Check embedded raw resource
            val rawName = item.fileName.substringBeforeLast(".")
            val resId = context.resources.getIdentifier(rawName, "raw", context.packageName)
            if (resId != 0) {
                val soundId = soundPool.load(context, resId, 1)
                loadedSoundIds[item.id] = soundId
            }
        }
    }

    /**
     * Plays a one-shot SFX cue if available.
     * Applies the current duck multiplier so SFX is quieter during voice narration.
     * Gracefully skips if the sound file was never loaded (missing from storage).
     */
    fun playSfx(sfxId: String) {
        val soundId = loadedSoundIds[sfxId] ?: return // Gracefully skip missing SFX
        try {
            val effectiveVolume = sfxVolume * sfxDuckMultiplier
            soundPool.play(soundId, effectiveVolume, effectiveVolume, 1, 0, 1.0f)
        } catch (_: Exception) {
            // Silently skip if SoundPool encounters any issue
        }
    }

    /**
     * Starts or switches active ambient loop track.
     */
    fun playAmbient(ambientItem: SfxItem) {
        if (currentAmbientId == ambientItem.id && ambientPlayer?.isPlaying == true) {
            return
        }

        stopAmbient()

        val audioUri = resolveAudioUri(ambientItem.fileName) ?: return
        try {
            ambientPlayer = MediaPlayer().apply {
                setDataSource(context, audioUri)
                isLooping = true
                setVolume(ambientVolume, ambientVolume)
                prepare()
                start()
            }
            currentAmbientId = ambientItem.id
        } catch (_: Exception) {}
    }

    /**
     * Pauses ambient loop audio.
     */
    fun pauseAmbient() {
        try {
            ambientPlayer?.pause()
        } catch (_: Exception) {}
    }

    /**
     * Resumes ambient loop audio if previously loaded.
     */
    fun resumeAmbient() {
        try {
            if (ambientPlayer?.isPlaying == false) {
                ambientPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    /**
     * Stops and releases current ambient loop player.
     */
    fun stopAmbient() {
        try {
            ambientPlayer?.stop()
            ambientPlayer?.release()
        } catch (_: Exception) {}
        ambientPlayer = null
        currentAmbientId = null
    }

    /**
     * Resolves URI for a given sound effect file from storage or raw resources.
     */
    private fun resolveAudioUri(fileName: String): Uri? {
        val extFile = File(getSfxDirectory(), fileName)
        if (extFile.exists() && extFile.length() > 0) {
            return Uri.fromFile(extFile)
        }

        val intFile = File(getInternalSfxDirectory(), fileName)
        if (intFile.exists() && intFile.length() > 0) {
            return Uri.fromFile(intFile)
        }

        val rawName = fileName.substringBeforeLast(".")
        val resId = context.resources.getIdentifier(rawName, "raw", context.packageName)
        if (resId != 0) {
            return Uri.parse("android.resource://${context.packageName}/$resId")
        }

        return null
    }

    /**
     * Counts how many of the 50 checklist items are physically detected on the device.
     */
    fun getDetectedCount(): Int {
        var count = 0
        for (item in SfxCatalog.items) {
            if (resolveAudioUri(item.fileName) != null) {
                count++
            }
        }
        return count
    }

    /**
     * Checks if a specific SFX item is detected.
     */
    fun isItemAvailable(item: SfxItem): Boolean {
        return resolveAudioUri(item.fileName) != null
    }

    fun release() {
        stopAmbient()
        soundPool.release()
    }
}
