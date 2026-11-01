package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StandbySettings(
    val activeProfileId: String = "",
    val is24Hour: Boolean = true,
    val showSeconds: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL,
    val dateFormat: String = "EEE, MMM d",
    val additionalTimeZones: List<String> = emptyList(),
    val pageAutoRotateEnabled: Boolean = false,
    val pageAutoRotateIntervalSeconds: Int = 30,
    val tapAction: GestureAction = GestureAction.SHOW_CONTROLS,
    val swipeLeftAction: GestureAction = GestureAction.NEXT_PAGE,
    val swipeRightAction: GestureAction = GestureAction.PREVIOUS_PAGE,
    val swipeUpAction: GestureAction = GestureAction.TOGGLE_DIM,
    val exitOnUnplug: Boolean = true,
    val keepScreenOn: Boolean = true,
    val showOnLockScreen: Boolean = true,
    val photoFolderUri: String? = null,
    val photoIntervalSeconds: Int = 10,
    val kenBurnsEnabled: Boolean = true,
    val notificationPrivacyMode: Boolean = false,
    val weatherLocationLat: Double? = null,
    val weatherLocationLon: Double? = null,
    val weatherLocationName: String? = null,
    val weatherUpdateIntervalMinutes: Int = 30,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS
)
