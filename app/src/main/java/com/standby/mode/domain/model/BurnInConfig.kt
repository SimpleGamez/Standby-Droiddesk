package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BurnInConfig(
    val pixelShiftEnabled: Boolean = true,
    val pixelShiftAmount: Int = 4,
    val pixelShiftIntervalSeconds: Int = 60,
    val periodicMotionEnabled: Boolean = true,
    val autoDimEnabled: Boolean = true,
    val autoDimAfterMinutes: Int = 30,
    val screenOffEnabled: Boolean = false,
    val screenOffAfterMinutes: Int = 120
)
