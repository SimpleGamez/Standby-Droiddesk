package com.standby.mode.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.standby.mode.domain.model.BurnInConfig
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun BurnInProtectionWrapper(
    config: BurnInConfig,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!config.pixelShiftEnabled && !config.periodicMotionEnabled) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    var currentOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(config) {
        val intervalMs = (config.pixelShiftIntervalSeconds * 1000L).coerceAtLeast(5000L)
        val maxShift = config.pixelShiftAmount.toFloat()

        while (true) {
            delay(intervalMs)
            val randomX = (Random.nextFloat() * 2f - 1f) * maxShift
            val randomY = (Random.nextFloat() * 2f - 1f) * maxShift
            currentOffset = Offset(randomX, randomY)
        }
    }

    val animatedOffset by animateOffsetAsState(
        targetValue = currentOffset,
        animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
        label = "burn_in_shift"
    )

    Box(
        modifier = modifier.offset(
            x = animatedOffset.x.dp,
            y = animatedOffset.y.dp
        )
    ) {
        content()
    }
}

