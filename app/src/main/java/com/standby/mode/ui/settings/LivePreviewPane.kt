package com.standby.mode.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.mode.domain.model.BatteryInfo
import com.standby.mode.domain.model.StandbyProfile
import com.standby.mode.domain.model.StandbySettings
import com.standby.mode.ui.modules.ClockModule

@Composable
fun LivePreviewPane(
    profile: StandbyProfile?,
    settings: StandbySettings,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .background(
                if (profile?.themeConfig?.useGradientBackground == true &&
                    profile.themeConfig.gradientColors.size >= 2
                ) {
                    Brush.verticalGradient(profile.themeConfig.gradientColors.map { Color(it) })
                } else {
                    Brush.verticalGradient(listOf(Color.Black, Color(0xFF0F0F1A)))
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Render scaled preview
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(0.85f),
            contentAlignment = Alignment.Center
        ) {
            ClockModule(
                style = settings.clockStyle,
                is24Hour = settings.is24Hour,
                showSeconds = settings.showSeconds,
                dateFormat = settings.dateFormat,
                additionalTimeZones = settings.additionalTimeZones
            )
        }

        // Live badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "LIVE PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

