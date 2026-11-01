package com.standby.mode.domain.provider

import com.standby.mode.domain.model.MediaInfo
import kotlinx.coroutines.flow.Flow

interface MediaSessionProvider {
    fun getActiveMediaInfo(): Flow<MediaInfo?>
    fun getCurrentMediaInfo(): MediaInfo?
    fun sendPlayPause()
    fun sendSkipNext()
    fun sendSkipPrevious()
    fun sendSeekTo(positionMs: Long)
}
