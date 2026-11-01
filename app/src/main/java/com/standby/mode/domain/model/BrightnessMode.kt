package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BrightnessMode {
    MANUAL, AUTO_SENSOR, SCHEDULED
}
