package com.standby.mode.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.standby.mode.domain.model.NightMode
import com.standby.mode.domain.model.ThemeConfig

// ──────────────────────────────────────────────
// Extra semantic colours that sit alongside the
// Material 3 ColorScheme
// ──────────────────────────────────────────────
@Immutable
data class StandbyExtraColors(
    val gradientStart: Color = Color.Transparent,
    val gradientEnd: Color = Color.Transparent,
    val nightModeTint: Color = Color.Transparent,
    val clockColor: Color = Color.Unspecified,
    val dimOverlay: Color = Color.Black.copy(alpha = 0.5f),
)

val LocalStandbyColors = staticCompositionLocalOf { StandbyExtraColors() }

/**
 * Application-level Material 3 theme.
 *
 * @param themeConfig settings chosen by the user (palette, font, etc.).
 * @param nightMode  current night mode state.
 * @param content    composable content slot.
 */
@Composable
fun StandByTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    nightMode: NightMode = NightMode.OFF,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Always prefer dark in StandBy (bedside use) but respect explicit light config
    val useDark = true.also { _ -> darkTheme } // StandBy is inherently dark

    val colorScheme = getColorScheme(
        palette = themeConfig.colorPalette,
        isDark = useDark,
        context = context,
    )

    val typography = buildTypography(
        fontFamily = themeConfig.fontFamily,
        fontSize = themeConfig.fontSize,
        fontWeight = themeConfig.fontWeight,
        letterSpacing = themeConfig.letterSpacing,
    )

    // Build extra colours
    val gradientStart = if (themeConfig.gradientColors.isNotEmpty())
        Color(themeConfig.gradientColors.first()) else colorScheme.surface
    val gradientEnd = if (themeConfig.gradientColors.size > 1)
        Color(themeConfig.gradientColors.last()) else colorScheme.surfaceVariant

    val nightTint = when (nightMode) {
        NightMode.RED_SHIFT -> Color(0x66FF1744)
        NightMode.ULTRA_DIM -> Color(0xCC000000)
        NightMode.AUTO, NightMode.OFF -> Color.Transparent
    }

    val extraColors = StandbyExtraColors(
        gradientStart = gradientStart,
        gradientEnd = gradientEnd,
        nightModeTint = nightTint,
        clockColor = colorScheme.primary,
        dimOverlay = Color.Black.copy(alpha = 0.5f),
    )

    // System bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalStandbyColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = StandByShapes,
            content = content,
        )
    }
}
