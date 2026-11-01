package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ModuleType {
    CLOCK, CALENDAR, WEATHER, BATTERY, PHOTO_SLIDESHOW, MEDIA_CONTROLS,
    NOTIFICATIONS, QUICK_TOGGLES, TIMER, CUSTOM_TEXT, AMBIENT_VISUALS
}
