package com.standby.mode.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.standby.mode.domain.model.ColorPalette

// ──────────────────────────────────────────────
// AMOLED Black palette
// ──────────────────────────────────────────────
private val AmoledBlackDark = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1A1A1A),
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondary = Color(0xFFBBBBBB),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF111111),
    onSecondaryContainer = Color(0xFFBBBBBB),
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF0A0A0A),
    onTertiaryContainer = Color(0xFF80CBC4),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF0D0D0D),
    onSurfaceVariant = Color(0xFFC0C0C0),
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF222222),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF330000),
    onErrorContainer = Color(0xFFFF6B6B),
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF000000),
    inversePrimary = Color(0xFF333333),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFE0E0E0),
)

private val AmoledBlackLight = lightColorScheme(
    primary = Color(0xFF1A1A1A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF444444),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF444444),
    tertiary = Color(0xFF00695C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF00695C),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFF999999),
    outlineVariant = Color(0xFFCCCCCC),
)

// ──────────────────────────────────────────────
// Sunset palette
// ──────────────────────────────────────────────
private val SunsetDark = darkColorScheme(
    primary = Color(0xFFFFAB91),
    onPrimary = Color(0xFF3E1500),
    primaryContainer = Color(0xFF5C2100),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = Color(0xFFF48FB1),
    onSecondary = Color(0xFF4A0028),
    secondaryContainer = Color(0xFF6A0039),
    onSecondaryContainer = Color(0xFFFFD9E4),
    tertiary = Color(0xFFFFD54F),
    onTertiary = Color(0xFF3B2F00),
    tertiaryContainer = Color(0xFF554400),
    onTertiaryContainer = Color(0xFFFFE99D),
    background = Color(0xFF1A0E08),
    onBackground = Color(0xFFF5DED4),
    surface = Color(0xFF1A0E08),
    onSurface = Color(0xFFF5DED4),
    surfaceVariant = Color(0xFF53433D),
    onSurfaceVariant = Color(0xFFD8C2BA),
    outline = Color(0xFFA08D85),
    outlineVariant = Color(0xFF53433D),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFF5DED4),
    inverseOnSurface = Color(0xFF1A0E08),
    inversePrimary = Color(0xFF8E3600),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFAB91),
)

private val SunsetLight = lightColorScheme(
    primary = Color(0xFF8E3600),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF3E1500),
    secondary = Color(0xFF9C254D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E4),
    onSecondaryContainer = Color(0xFF4A0028),
    tertiary = Color(0xFF6D5E00),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFE99D),
    onTertiaryContainer = Color(0xFF3B2F00),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF231A15),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF231A15),
    surfaceVariant = Color(0xFFF5DED4),
    onSurfaceVariant = Color(0xFF53433D),
    outline = Color(0xFF85746C),
    outlineVariant = Color(0xFFD8C2BA),
)

// ──────────────────────────────────────────────
// Ocean palette
// ──────────────────────────────────────────────
private val OceanDark = darkColorScheme(
    primary = Color(0xFF80D8FF),
    onPrimary = Color(0xFF00344E),
    primaryContainer = Color(0xFF004C6E),
    onPrimaryContainer = Color(0xFFBFEAFF),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF004F49),
    onSecondaryContainer = Color(0xFFA2F0E8),
    tertiary = Color(0xFFB388FF),
    onTertiary = Color(0xFF2E0060),
    tertiaryContainer = Color(0xFF430087),
    onTertiaryContainer = Color(0xFFE0CCFF),
    background = Color(0xFF051E2B),
    onBackground = Color(0xFFD4E6F1),
    surface = Color(0xFF051E2B),
    onSurface = Color(0xFFD4E6F1),
    surfaceVariant = Color(0xFF3B4E58),
    onSurfaceVariant = Color(0xFFBBC9D3),
    outline = Color(0xFF86939D),
    outlineVariant = Color(0xFF3B4E58),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFD4E6F1),
    inverseOnSurface = Color(0xFF051E2B),
    inversePrimary = Color(0xFF006492),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF80D8FF),
)

private val OceanLight = lightColorScheme(
    primary = Color(0xFF006492),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFEAFF),
    onPrimaryContainer = Color(0xFF00344E),
    secondary = Color(0xFF006B63),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA2F0E8),
    onSecondaryContainer = Color(0xFF003733),
    tertiary = Color(0xFF6200EE),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0CCFF),
    onTertiaryContainer = Color(0xFF2E0060),
    background = Color(0xFFF0F8FF),
    onBackground = Color(0xFF0E1E28),
    surface = Color(0xFFF0F8FF),
    onSurface = Color(0xFF0E1E28),
    surfaceVariant = Color(0xFFD8E6F0),
    onSurfaceVariant = Color(0xFF3B4E58),
    outline = Color(0xFF6D808A),
    outlineVariant = Color(0xFFBBC9D3),
)

// ──────────────────────────────────────────────
// Forest palette
// ──────────────────────────────────────────────
private val ForestDark = darkColorScheme(
    primary = Color(0xFFA5D6A7),
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF005219),
    onPrimaryContainer = Color(0xFFC8F0C6),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF3B2E27),
    secondaryContainer = Color(0xFF52443D),
    onSecondaryContainer = Color(0xFFD8C5BE),
    tertiary = Color(0xFFFFF59D),
    onTertiary = Color(0xFF3A3300),
    tertiaryContainer = Color(0xFF534B00),
    onTertiaryContainer = Color(0xFFFFEFAA),
    background = Color(0xFF0B1A0E),
    onBackground = Color(0xFFDAE8D8),
    surface = Color(0xFF0B1A0E),
    onSurface = Color(0xFFDAE8D8),
    surfaceVariant = Color(0xFF3E4E3F),
    onSurfaceVariant = Color(0xFFBCCDBB),
    outline = Color(0xFF879787),
    outlineVariant = Color(0xFF3E4E3F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFDAE8D8),
    inverseOnSurface = Color(0xFF0B1A0E),
    inversePrimary = Color(0xFF006D24),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFA5D6A7),
)

private val ForestLight = lightColorScheme(
    primary = Color(0xFF006D24),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8F0C6),
    onPrimaryContainer = Color(0xFF003910),
    secondary = Color(0xFF6B5B53),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD8C5BE),
    onSecondaryContainer = Color(0xFF3B2E27),
    tertiary = Color(0xFF6C6400),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFEFAA),
    onTertiaryContainer = Color(0xFF3A3300),
    background = Color(0xFFF3F8F0),
    onBackground = Color(0xFF161E14),
    surface = Color(0xFFF3F8F0),
    onSurface = Color(0xFF161E14),
    surfaceVariant = Color(0xFFDAEAD7),
    onSurfaceVariant = Color(0xFF3E4E3F),
    outline = Color(0xFF6E806E),
    outlineVariant = Color(0xFFBCCDBB),
)

// ──────────────────────────────────────────────
// Custom / Default fallback (Material 3 baseline)
// ──────────────────────────────────────────────
private val DefaultDark = darkColorScheme()
private val DefaultLight = lightColorScheme()

/**
 * Resolves the [ColorScheme] for the given [palette] and dark-mode flag.
 *
 * When [palette] is [ColorPalette.MATERIAL_YOU], dynamic colours are used on
 * Android 12+ and the baseline Material 3 palette is used on older versions.
 */
fun getColorScheme(
    palette: ColorPalette,
    isDark: Boolean,
    context: Context,
): ColorScheme {
    return when (palette) {
        ColorPalette.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else {
                if (isDark) DefaultDark else DefaultLight
            }
        }

        ColorPalette.AMOLED_BLACK -> if (isDark) AmoledBlackDark else AmoledBlackLight
        ColorPalette.SUNSET -> if (isDark) SunsetDark else SunsetLight
        ColorPalette.OCEAN -> if (isDark) OceanDark else OceanLight
        ColorPalette.FOREST -> if (isDark) ForestDark else ForestLight
        ColorPalette.CUSTOM -> if (isDark) DefaultDark else DefaultLight
    }
}
