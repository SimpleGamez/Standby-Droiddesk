package com.standby.mode.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class StandbyProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Default",
    val pages: List<PageLayout> = emptyList(),
    val triggerConfig: TriggerConfig = TriggerConfig(),
    val themeConfig: ThemeConfig = ThemeConfig(),
    val brightnessConfig: BrightnessConfig = BrightnessConfig(),
    val burnInConfig: BurnInConfig = BurnInConfig(),
    val isActive: Boolean = false
)
