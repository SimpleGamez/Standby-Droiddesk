package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TriggerConfig(
    val onChargerConnected: Boolean = true,
    val onChargingAndLandscape: Boolean = false,
    val onChargingAndLocked: Boolean = false,
    val onChargingAndStationary: Boolean = false,
    val manualOnly: Boolean = false
)
