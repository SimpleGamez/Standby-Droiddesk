package com.standby.mode.data.local

import com.standby.mode.domain.model.BrightnessConfig
import com.standby.mode.domain.model.BrightnessMode
import com.standby.mode.domain.model.BurnInConfig
import com.standby.mode.domain.model.ColorPalette
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.NightMode
import com.standby.mode.domain.model.PageLayout
import com.standby.mode.domain.model.StandbyModule
import com.standby.mode.domain.model.StandbyProfile
import com.standby.mode.domain.model.ThemeConfig
import com.standby.mode.domain.model.TriggerConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun ProfileEntity.toDomain(pages: List<PageLayout>): StandbyProfile {
    return StandbyProfile(
        id = id,
        name = name,
        pages = pages,
        triggerConfig = TriggerConfig(
            onChargerConnected = triggerOnChargerConnected,
            onChargingAndLandscape = triggerOnChargingAndLandscape,
            onChargingAndLocked = triggerOnChargingAndLocked,
            onChargingAndStationary = triggerOnChargingAndStationary,
            manualOnly = triggerManualOnly
        ),
        themeConfig = ThemeConfig(
            colorPalette = runCatching { ColorPalette.valueOf(themeColorPalette) }.getOrDefault(ColorPalette.MATERIAL_YOU),
            customAccentColor = themeCustomAccentColor,
            useGradientBackground = themeUseGradientBackground,
            gradientColors = runCatching { json.decodeFromString<List<Long>>(themeGradientColors) }.getOrDefault(emptyList()),
            fontFamily = themeFontFamily,
            fontSize = themeFontSize,
            fontWeight = themeFontWeight,
            letterSpacing = themeLetterSpacing,
            useDynamicColor = themeUseDynamicColor
        ),
        brightnessConfig = BrightnessConfig(
            mode = runCatching { BrightnessMode.valueOf(brightnessMode) }.getOrDefault(BrightnessMode.AUTO_SENSOR),
            manualLevel = brightnessManualLevel,
            scheduledDimTime = brightnessScheduledDimTime,
            scheduledBrightTime = brightnessScheduledBrightTime,
            dimLevel = brightnessDimLevel,
            nightMode = runCatching { NightMode.valueOf(brightnessNightMode) }.getOrDefault(NightMode.OFF),
            nightStartTime = brightnessNightStartTime,
            nightEndTime = brightnessNightEndTime
        ),
        burnInConfig = BurnInConfig(
            pixelShiftEnabled = burnInPixelShiftEnabled,
            pixelShiftAmount = burnInPixelShiftAmount,
            pixelShiftIntervalSeconds = burnInPixelShiftIntervalSeconds,
            periodicMotionEnabled = burnInPeriodicMotionEnabled,
            autoDimEnabled = burnInAutoDimEnabled,
            autoDimAfterMinutes = burnInAutoDimAfterMinutes,
            screenOffEnabled = burnInScreenOffEnabled,
            screenOffAfterMinutes = burnInScreenOffAfterMinutes
        ),
        isActive = isActive
    )
}

fun StandbyProfile.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = id,
        name = name,
        isActive = isActive,
        triggerOnChargerConnected = triggerConfig.onChargerConnected,
        triggerOnChargingAndLandscape = triggerConfig.onChargingAndLandscape,
        triggerOnChargingAndLocked = triggerConfig.onChargingAndLocked,
        triggerOnChargingAndStationary = triggerConfig.onChargingAndStationary,
        triggerManualOnly = triggerConfig.manualOnly,
        themeColorPalette = themeConfig.colorPalette.name,
        themeCustomAccentColor = themeConfig.customAccentColor,
        themeUseGradientBackground = themeConfig.useGradientBackground,
        themeGradientColors = json.encodeToString(themeConfig.gradientColors),
        themeFontFamily = themeConfig.fontFamily,
        themeFontSize = themeConfig.fontSize,
        themeFontWeight = themeConfig.fontWeight,
        themeLetterSpacing = themeConfig.letterSpacing,
        themeUseDynamicColor = themeConfig.useDynamicColor,
        brightnessMode = brightnessConfig.mode.name,
        brightnessManualLevel = brightnessConfig.manualLevel,
        brightnessScheduledDimTime = brightnessConfig.scheduledDimTime,
        brightnessScheduledBrightTime = brightnessConfig.scheduledBrightTime,
        brightnessDimLevel = brightnessConfig.dimLevel,
        brightnessNightMode = brightnessConfig.nightMode.name,
        brightnessNightStartTime = brightnessConfig.nightStartTime,
        brightnessNightEndTime = brightnessConfig.nightEndTime,
        burnInPixelShiftEnabled = burnInConfig.pixelShiftEnabled,
        burnInPixelShiftAmount = burnInConfig.pixelShiftAmount,
        burnInPixelShiftIntervalSeconds = burnInConfig.pixelShiftIntervalSeconds,
        burnInPeriodicMotionEnabled = burnInConfig.periodicMotionEnabled,
        burnInAutoDimEnabled = burnInConfig.autoDimEnabled,
        burnInAutoDimAfterMinutes = burnInConfig.autoDimAfterMinutes,
        burnInScreenOffEnabled = burnInConfig.screenOffEnabled,
        burnInScreenOffAfterMinutes = burnInConfig.screenOffAfterMinutes
    )
}

fun PageLayout.toEntity(profileId: String): PageEntity {
    return PageEntity(
        id = id,
        profileId = profileId,
        name = name,
        sortOrder = sortOrder
    )
}

fun PageEntity.toDomain(modules: List<StandbyModule>): PageLayout {
    return PageLayout(
        id = id,
        name = name,
        modules = modules,
        sortOrder = sortOrder
    )
}

fun StandbyModule.toEntity(pageId: String): ModuleEntity {
    return ModuleEntity(
        id = id,
        pageId = pageId,
        type = type.name,
        row = row,
        column = column,
        rowSpan = rowSpan,
        colSpan = colSpan,
        config = json.encodeToString(config)
    )
}

fun ModuleEntity.toDomain(): StandbyModule {
    return StandbyModule(
        id = id,
        type = runCatching { ModuleType.valueOf(type) }.getOrDefault(ModuleType.CLOCK),
        row = row,
        column = column,
        rowSpan = rowSpan,
        colSpan = colSpan,
        config = runCatching { json.decodeFromString<Map<String, String>>(config) }.getOrDefault(emptyMap())
    )
}
