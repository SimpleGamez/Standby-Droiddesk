package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BatteryInfo(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val chargeType: ChargeType = ChargeType.NONE,
    val temperature: Float = 0f,
    val voltage: Float = 0f,
    val currentNow: Int = 0,
    val estimatedTimeToFull: Long? = null
)
