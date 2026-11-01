package com.standby.mode.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.standby.mode.domain.model.NightMode

@Composable
fun NightModeOverlay(
    nightMode: NightMode,
    isNightActive: Boolean,
    modifier: Modifier = Modifier
) {
    val shouldShow = isNightActive && nightMode != NightMode.OFF

    val overlayColor = when (nightMode) {
        NightMode.RED_SHIFT -> Color(0x66FF1744) // Red tint for night preservation
        NightMode.ULTRA_DIM -> Color(0xCC000000) // Deep black overlay
        NightMode.AUTO -> Color(0x55FF3D00)
        NightMode.OFF -> Color.Transparent
    }

    AnimatedVisibility(
        visible = shouldShow,
        enter = fadeIn(animationSpec = tween(1200)),
        exit = fadeOut(animationSpec = tween(600)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
        )
    }
}

