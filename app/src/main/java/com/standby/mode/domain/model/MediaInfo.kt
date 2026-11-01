package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MediaInfo(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtUri: String? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val packageName: String = ""
)
