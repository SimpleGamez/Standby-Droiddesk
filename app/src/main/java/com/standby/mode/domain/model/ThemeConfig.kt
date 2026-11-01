package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ThemeConfig(
    val colorPalette: ColorPalette = ColorPalette.MATERIAL_YOU,
    val customAccentColor: Long? = null,
    val useGradientBackground: Boolean = false,
    val gradientColors: List<Long> = emptyList(),
    val fontFamily: String = "sans-serif",
    val fontSize: Float = 64f,
    val fontWeight: Int = 400,
    val letterSpacing: Float = 0f,
    val useDynamicColor: Boolean = true
)
