package com.standby.mode.ui.modules

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.mode.domain.model.BatteryInfo
import com.standby.mode.domain.model.ChargeType
import java.util.Locale
import kotlin.math.abs

@Composable
fun BatteryModule(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "charging")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val gaugeColor = when {
        batteryInfo.level <= 15 -> Color(0xFFFF5252)
        batteryInfo.isCharging -> Color(0xFF69F0AE)
        batteryInfo.level <= 30 -> Color(0xFFFFD740)
        else -> MaterialTheme.colorScheme.primary
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    // Calculate charge speed in Watts (voltage * current)
    // currentNow is typically in microamperes (uA), voltage in Volts
    val watts = if (batteryInfo.isCharging && batteryInfo.currentNow != 0) {
        val currentAmps = abs(batteryInfo.currentNow) / 1_000_000.0f
        val calculated = batteryInfo.voltage * currentAmps
        if (calculated in 0.5f..150.0f) calculated else null
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset(
                    (size.width - radius * 2) / 2f,
                    (size.height - radius * 2) / 2f
                )

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Fill sweep
                val sweep = (batteryInfo.level / 100f) * 360f
                val activeColor = if (batteryInfo.isCharging) {
                    gaugeColor.copy(alpha = pulseAlpha)
                } else gaugeColor

                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${batteryInfo.level}%",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (batteryInfo.isCharging) {
                    Text(
                        text = "⚡ CHARGING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = gaugeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stats row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (watts != null) {
                StatItem(label = "SPEED", value = String.format(Locale.US, "%.1f W", watts))
            } else if (batteryInfo.chargeType != ChargeType.NONE) {
                StatItem(label = "SOURCE", value = batteryInfo.chargeType.name)
            }

            if (batteryInfo.temperature > 0) {
                StatItem(
                    label = "TEMP",
                    value = String.format(Locale.US, "%.1f°C", batteryInfo.temperature)
                )
            }

            if (batteryInfo.estimatedTimeToFull != null && batteryInfo.estimatedTimeToFull > 0) {
                val mins = batteryInfo.estimatedTimeToFull / (1000 * 60)
                val hours = mins / 60
                val remainingMins = mins % 60
                val timeStr = if (hours > 0) "${hours}h ${remainingMins}m" else "${remainingMins}m"
                StatItem(label = "FULL IN", value = timeStr)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

