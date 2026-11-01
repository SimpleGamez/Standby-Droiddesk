package com.standby.mode.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val isActive: Boolean,
    val triggerOnChargerConnected: Boolean,
    val triggerOnChargingAndLandscape: Boolean,
    val triggerOnChargingAndLocked: Boolean,
    val triggerOnChargingAndStationary: Boolean,
    val triggerManualOnly: Boolean,
    val themeColorPalette: String,
    val themeCustomAccentColor: Long?,
    val themeUseGradientBackground: Boolean,
    val themeGradientColors: String,
    val themeFontFamily: String,
    val themeFontSize: Float,
    val themeFontWeight: Int,
    val themeLetterSpacing: Float,
    val themeUseDynamicColor: Boolean,
    val brightnessMode: String,
    val brightnessManualLevel: Float,
    val brightnessScheduledDimTime: String,
    val brightnessScheduledBrightTime: String,
    val brightnessDimLevel: Float,
    val brightnessNightMode: String,
    val brightnessNightStartTime: String,
    val brightnessNightEndTime: String,
    val burnInPixelShiftEnabled: Boolean,
    val burnInPixelShiftAmount: Int,
    val burnInPixelShiftIntervalSeconds: Int,
    val burnInPeriodicMotionEnabled: Boolean,
    val burnInAutoDimEnabled: Boolean,
    val burnInAutoDimAfterMinutes: Int,
    val burnInScreenOffEnabled: Boolean,
    val burnInScreenOffAfterMinutes: Int
)
