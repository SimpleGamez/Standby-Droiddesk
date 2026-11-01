package com.standby.mode.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import com.standby.mode.domain.model.MediaInfo
import com.standby.mode.domain.provider.MediaSessionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaSessionProvider {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _mediaInfo = MutableStateFlow<MediaInfo?>(null)
    override fun getActiveMediaInfo(): Flow<MediaInfo?> = _mediaInfo.asStateFlow()
    override fun getCurrentMediaInfo(): MediaInfo? = _mediaInfo.value

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var activeController: MediaController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateMediaInfo()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateMediaInfo()
        }

        override fun onSessionDestroyed() {
            activeController = null
            // Only clear if no broadcast info exists
            if (activeController == null && _mediaInfo.value?.packageName?.contains("spotify") != true) {
                _mediaInfo.value = null
            }
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers)
    }

    private val musicBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val action = intent.action ?: return

            if (action == "com.spotify.music.playbackstatechanged" ||
                action.endsWith(".playstatechanged") ||
                action.endsWith(".ACTION_PAUSED")
            ) {
                val isPlaying = if (action.endsWith(".ACTION_PAUSED")) {
                    false
                } else {
                    intent.getBooleanExtra("playing", intent.getBooleanExtra("isPlaying", false))
                }
                val pos = intent.getIntExtra("playbackPosition", intent.getIntExtra("position", 0)).toLong()
                _mediaInfo.value = _mediaInfo.value?.copy(
                    isPlaying = isPlaying,
                    position = if (pos > 0) pos else (_mediaInfo.value?.position ?: 0L)
                )
            } else {
                val track = intent.getStringExtra("track")
                    ?: intent.getStringExtra("title") ?: ""
                val artist = intent.getStringExtra("artist") ?: ""
                val album = intent.getStringExtra("album") ?: ""
                val durationSec = intent.getIntExtra("length", intent.getIntExtra("duration", 0))
                val durationMs = if (durationSec in 1..9999) durationSec * 1000L else durationSec.toLong()
                val isPlaying = intent.getBooleanExtra("playing", intent.getBooleanExtra("isPlaying", true))

                if (track.isNotBlank() || artist.isNotBlank()) {
                    _mediaInfo.value = MediaInfo(
                        title = track,
                        artist = artist,
                        album = album,
                        albumArtUri = null,
                        isPlaying = isPlaying,
                        position = 0L,
                        duration = durationMs,
                        packageName = intent.`package` ?: "com.spotify.music"
                    )
                }
            }
        }
    }

    private var isListening = false

    init {
        StandbyNotificationListenerService.onConnectedCallback = {
            mainHandler.post { refreshSessions() }
        }
        startListening()
    }

    fun startListening() {
        if (isListening) return

        // 1. Register broadcast receiver fallback for Spotify, Samsung Music, HTC, Sony, Apple Music
        val filter = IntentFilter().apply {
            addAction("com.spotify.music.metadatachanged")
            addAction("com.spotify.music.playbackstatechanged")
            addAction("com.spotify.music.queuechanged")
            addAction("com.android.music.metadatachanged")
            addAction("com.android.music.playstatechanged")
            addAction("com.sec.android.app.music.metadatachanged")
            addAction("com.sec.android.app.music.playstatechanged")
            addAction("com.htc.music.metadatachanged")
            addAction("com.real.IMP.metadatachanged")
            addAction("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED")
            addAction("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED")
            addAction("com.apple.android.music.metadatachanged")
            addAction("fm.last.android.metadatachanged")
        }
        try {
            ContextCompat.registerReceiver(
                context,
                musicBroadcastReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            // Ignore
        }

        // 2. Connect to system MediaSessionManager
        refreshSessions()
        isListening = true
    }

    fun refreshSessions() {
        if (mediaSessionManager == null) return
        try {
            val component = ComponentName(context, StandbyNotificationListenerService::class.java)
            val controllers = mediaSessionManager.getActiveSessions(component)
            updateActiveController(controllers)
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionListener)
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, component, mainHandler)
        } catch (e: SecurityException) {
            // NotificationListener permission not granted yet; broadcast receiver handles Spotify
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            context.unregisterReceiver(musicBroadcastReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
            activeController?.unregisterCallback(controllerCallback)
            activeController = null
        } catch (e: Exception) {
            // Ignore
        }
        isListening = false
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        val newController = controllers?.firstOrNull() ?: return
        activeController?.unregisterCallback(controllerCallback)
        activeController = newController
        activeController?.registerCallback(controllerCallback, mainHandler)
        updateMediaInfo()
    }

    private fun updateMediaInfo() {
        val controller = activeController ?: return

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = playbackState?.position ?: 0L
        var artUri = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)

        if (title.isNotEmpty() || artist.isNotEmpty()) {
            // If URI is null, check for embedded Bitmap (common in Spotify/YouTube)
            if (artUri.isNullOrBlank()) {
                val bitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                if (bitmap != null) {
                    scope.launch {
                        val cachedUri = cacheBitmap(bitmap)
                        if (cachedUri != null) {
                            _mediaInfo.value = _mediaInfo.value?.copy(albumArtUri = cachedUri)
                        }
                    }
                }
            }

            _mediaInfo.value = MediaInfo(
                title = title,
                artist = artist,
                album = album,
                albumArtUri = artUri,
                isPlaying = isPlaying,
                position = position,
                duration = duration,
                packageName = controller.packageName ?: ""
            )
        }
    }

    private fun cacheBitmap(bitmap: Bitmap): String? {
        return try {
            val file = File(context.cacheDir, "current_media_art.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file.toURI().toString()
        } catch (e: Exception) {
            null
        }
    }

    override fun sendPlayPause() {
        val controller = activeController
        if (controller != null) {
            val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
            if (isPlaying) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        } else {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
            _mediaInfo.value?.let { current ->
                _mediaInfo.value = current.copy(isPlaying = !current.isPlaying)
            }
        }
    }

    override fun sendSkipNext() {
        val controller = activeController
        if (controller != null) {
            controller.transportControls.skipToNext()
        } else {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
        }
    }

    override fun sendSkipPrevious() {
        val controller = activeController
        if (controller != null) {
            controller.transportControls.skipToPrevious()
        } else {
            dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        }
    }

    override fun sendSeekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs)
    }

    private fun dispatchMediaKey(keyCode: Int) {
        try {
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager?.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
