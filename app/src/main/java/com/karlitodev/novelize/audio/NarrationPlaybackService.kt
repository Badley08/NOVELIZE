package com.karlitodev.novelize.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.karlitodev.novelize.MainActivity

/**
 * Foreground service ensuring uninterrupted audio narration when the screen is turned off
 * and providing standard system media playback controls in the Android status bar and lock screen.
 */
class NarrationPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "novelize_playback_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_PLAY = "com.karlitodev.novelize.ACTION_PLAY"
        const val ACTION_PAUSE = "com.karlitodev.novelize.ACTION_PAUSE"
        const val ACTION_NEXT = "com.karlitodev.novelize.ACTION_NEXT"
        const val ACTION_PREV = "com.karlitodev.novelize.ACTION_PREV"
        const val ACTION_STOP = "com.karlitodev.novelize.ACTION_STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_IS_PLAYING = "extra_is_playing"

        var playbackController: PlaybackController? = null

        fun startService(context: Context, title: String, subtitle: String, isPlaying: Boolean) {
            val intent = Intent(context, NarrationPlaybackService::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NarrationPlaybackService::class.java)
            context.stopService(intent)
        }
    }

    interface PlaybackController {
        fun onPlay()
        fun onPause()
        fun onNext()
        fun onPrev()
        fun onStop()
    }

    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(this, "NoveLizePlaybackSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    playbackController?.onPlay()
                }

                override fun onPause() {
                    playbackController?.onPause()
                }

                override fun onSkipToNext() {
                    playbackController?.onNext()
                }

                override fun onSkipToPrevious() {
                    playbackController?.onPrev()
                }

                override fun onStop() {
                    playbackController?.onStop()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> playbackController?.onPlay()
            ACTION_PAUSE -> playbackController?.onPause()
            ACTION_NEXT -> playbackController?.onNext()
            ACTION_PREV -> playbackController?.onPrev()
            ACTION_STOP -> {
                playbackController?.onStop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "NoveLize Reader"
        val subtitle = intent?.getStringExtra(EXTRA_SUBTITLE) ?: "Voice Narration Active"
        val isPlaying = intent?.getBooleanExtra(EXTRA_IS_PLAYING, true) ?: true

        updateMediaSessionState(title, subtitle, isPlaying)
        val notification = buildNotification(title, subtitle, isPlaying)
        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    private fun updateMediaSessionState(title: String, subtitle: String, isPlaying: Boolean) {
        val session = mediaSession ?: return

        val state = if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_STOP

        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .setActions(actions)
                .build()
        )

        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "NoveLize")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, subtitle)
                .build()
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(title: String, subtitle: String, isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, NarrationPlaybackService::class.java).apply { action = ACTION_PREV }
        val prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIntent = Intent(this, NarrationPlaybackService::class.java).apply { action = playPauseAction }
        val playPausePendingIntent = PendingIntent.getService(this, 2, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, NarrationPlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, NarrationPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 4, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val sessionToken = mediaSession?.sessionToken

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSmallIcon(if (isPlaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPendingIntent)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                playPausePendingIntent
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (sessionToken != null) {
            builder.setStyle(
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopPendingIntent)
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Narration",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for background light novel voice narration"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
