package com.standby.mode.service

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.standby.mode.domain.model.MediaInfo
import com.standby.mode.domain.provider.MediaSessionProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaSessionMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaSessionProvider {

    private val _mediaInfo = MutableStateFlow<MediaInfo?>(null)
    override fun getActiveMediaInfo(): Flow<MediaInfo?> = _mediaInfo.asStateFlow()
    override fun getCurrentMediaInfo(): MediaInfo? = _mediaInfo.value

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

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
            _mediaInfo.value = null
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveController(controllers)
    }

    private var isListening = false

    fun startListening() {
        if (isListening || mediaSessionManager == null) return
        try {
            val component = ComponentName(context, StandbyNotificationListenerService::class.java)
            val controllers = mediaSessionManager.getActiveSessions(component)
            updateActiveController(controllers)
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionListener, component)
            isListening = true
        } catch (e: SecurityException) {
            // NotificationListener permission not granted yet
            _mediaInfo.value = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening() {
        if (!isListening) return
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
        activeController?.unregisterCallback(controllerCallback)
        activeController = controllers?.firstOrNull()
        activeController?.registerCallback(controllerCallback, mainHandler)
        updateMediaInfo()
    }

    private fun updateMediaInfo() {
        val controller = activeController ?: run {
            _mediaInfo.value = null
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = playbackState?.position ?: 0L
        val artUri = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)

        if (title.isNotEmpty() || artist.isNotEmpty()) {
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
        } else {
            _mediaInfo.value = null
        }
    }

    override fun sendPlayPause() {
        activeController?.let { controller ->
            val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
            if (isPlaying) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        }
    }

    override fun sendSkipNext() {
        activeController?.transportControls?.skipToNext()
    }

    override fun sendSkipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    override fun sendSeekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs)
    }
}

