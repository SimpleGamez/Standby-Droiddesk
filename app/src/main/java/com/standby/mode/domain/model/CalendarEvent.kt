package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: Long = 0L,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val color: Int = 0,
    val location: String? = null,
    val allDay: Boolean = false
)

