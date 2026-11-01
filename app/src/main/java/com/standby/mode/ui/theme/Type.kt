package com.standby.mode.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Maps a font family preference string to a Compose [FontFamily].
 */
fun resolveFontFamily(name: String): FontFamily {
    return when (name.lowercase()) {
        "monospace" -> FontFamily.Monospace
        "serif" -> FontFamily.Serif
        "cursive" -> FontFamily.Cursive
        "sans-serif", "default", "" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
}

/**
 * Builds a complete [Typography] scaled from the user-chosen font settings.
 *
 * @param fontFamily family key ("sans-serif", "serif", "monospace", "clock").
 * @param fontSize   base display-large size in sp. Other styles scale relative
 *                   to it so the entire hierarchy stays proportional.
 * @param fontWeight weight (100-900) applied to display / headline styles.
 * @param letterSpacing extra spacing in sp applied to every style.
 */
fun buildTypography(
    fontFamily: String = "sans-serif",
    fontSize: Float = 64f,
    fontWeight: Int = 400,
    letterSpacing: Float = 0f,
): Typography {
    val family = resolveFontFamily(fontFamily)
    val weight = FontWeight(fontWeight.coerceIn(100, 900))
    val spacing = letterSpacing.sp

    // Proportional scale: display large = base, everything else is a fraction.
    val baseSp = fontSize.sp

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = baseSp,
            lineHeight = baseSp * 1.12f,
            letterSpacing = (letterSpacing - 0.25f).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = baseSp * 0.703f,   // ~45 sp @ 64
            lineHeight = baseSp * 0.813f,
            letterSpacing = spacing,
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = baseSp * 0.5625f,  // ~36 sp @ 64
            lineHeight = baseSp * 0.6875f,
            letterSpacing = spacing,
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = baseSp * 0.5f,     // ~32 sp @ 64
            lineHeight = baseSp * 0.625f,
            letterSpacing = spacing,
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = baseSp * 0.4375f,  // ~28 sp @ 64
            lineHeight = baseSp * 0.5625f,
            letterSpacing = spacing,
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = baseSp * 0.375f,   // ~24 sp @ 64
            lineHeight = baseSp * 0.5f,
            letterSpacing = spacing,
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = baseSp * 0.34375f, // ~22 sp @ 64
            lineHeight = baseSp * 0.4375f,
            letterSpacing = spacing,
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = baseSp * 0.25f,    // ~16 sp @ 64
            lineHeight = baseSp * 0.375f,
            letterSpacing = (letterSpacing + 0.15f).sp,
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = baseSp * 0.21875f, // ~14 sp @ 64
            lineHeight = baseSp * 0.3125f,
            letterSpacing = (letterSpacing + 0.1f).sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = baseSp * 0.25f,
            lineHeight = baseSp * 0.375f,
            letterSpacing = (letterSpacing + 0.5f).sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = baseSp * 0.21875f,
            lineHeight = baseSp * 0.3125f,
            letterSpacing = (letterSpacing + 0.25f).sp,
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = baseSp * 0.1875f,  // ~12 sp @ 64
            lineHeight = baseSp * 0.25f,
            letterSpacing = (letterSpacing + 0.4f).sp,
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = baseSp * 0.21875f,
            lineHeight = baseSp * 0.3125f,
            letterSpacing = (letterSpacing + 0.1f).sp,
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = baseSp * 0.1875f,
            lineHeight = baseSp * 0.25f,
            letterSpacing = (letterSpacing + 0.5f).sp,
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = baseSp * 0.171875f, // ~11 sp @ 64
            lineHeight = baseSp * 0.25f,
            letterSpacing = (letterSpacing + 0.5f).sp,
        ),
    )
}
