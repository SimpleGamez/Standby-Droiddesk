package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BrightnessConfig(
    val mode: BrightnessMode = BrightnessMode.AUTO_SENSOR,
    val manualLevel: Float = 0.5f,
    val scheduledDimTime: String = "22:00",
    val scheduledBrightTime: String = "07:00",
    val dimLevel: Float = 0.1f,
    val nightMode: NightMode = NightMode.OFF,
    val nightStartTime: String = "22:00",
    val nightEndTime: String = "06:00"
)
