package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val category: String? = null
)

