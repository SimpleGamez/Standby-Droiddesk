package com.standby.mode.ui.modules

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.mode.domain.model.ClockStyle
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockModule(
    style: ClockStyle = ClockStyle.DIGITAL,
    is24Hour: Boolean = true,
    showSeconds: Boolean = false,
    dateFormat: String = "EEEE, MMMM d",
    additionalTimeZones: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }

    LaunchedEffect(showSeconds) {
        while (true) {
            currentTime = ZonedDateTime.now()
            delay(if (showSeconds) 1000L else 15000L)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (style) {
                ClockStyle.DIGITAL -> DigitalClock(currentTime, is24Hour, showSeconds)
                ClockStyle.ANALOG -> AnalogClock(currentTime, showSeconds)
                ClockStyle.FLIP -> FlipClock(currentTime, is24Hour, showSeconds)
                ClockStyle.MINIMAL -> MinimalClock(currentTime, is24Hour)
                ClockStyle.BOLD -> BoldClock(currentTime, is24Hour)
                ClockStyle.WORD_CLOCK -> WordClock(currentTime)
                ClockStyle.BINARY -> BinaryClock(currentTime)
                ClockStyle.VERTICAL_STACK -> VerticalStackClock(currentTime, is24Hour)
            }

            if (dateFormat.isNotBlank()) {
                val formattedDate = remember(currentTime, dateFormat) {
                    try {
                        currentTime.format(DateTimeFormatter.ofPattern(dateFormat))
                    } catch (e: Exception) {
                        currentTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        letterSpacing = 1.2.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            if (additionalTimeZones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    additionalTimeZones.take(3).forEach { zoneStr ->
                        val zoneTime = remember(currentTime, zoneStr) {
                            try {
                                val zone = ZoneId.of(zoneStr)
                                ZonedDateTime.now(zone).format(
                                    DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a")
                                )
                            } catch (e: Exception) {
                                "--:--"
                            }
                        }
                        val cleanName = zoneStr.substringAfterLast("/").replace("_", " ")
                        Text(
                            text = "$cleanName: $zoneTime",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalClock(time: ZonedDateTime, is24Hour: Boolean, showSeconds: Boolean) {
    val pattern = if (is24Hour) {
        if (showSeconds) "HH:mm:ss" else "HH:mm"
    } else {
        if (showSeconds) "h:mm:ss a" else "h:mm a"
    }
    val timeText = time.format(DateTimeFormatter.ofPattern(pattern))

    Text(
        text = timeText,
        fontSize = 76.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = (-1.5).sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AnalogClock(time: ZonedDateTime, showSeconds: Boolean) {
    val hour = time.hour % 12
    val minute = time.minute
    val second = time.second

    val dialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    val handColor = MaterialTheme.colorScheme.primary
    val secondHandColor = MaterialTheme.colorScheme.tertiary
    val centerDotColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(200.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.width / 2f - 12.dp.toPx()

        // Clock Dial Background
        drawCircle(
            color = dialColor,
            radius = radius,
            center = center
        )
        drawCircle(
            color = handColor.copy(alpha = 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // 12 Hour Ticks
        for (i in 0 until 12) {
            val angle = (i * 30.0 - 90.0) * (PI / 180.0)
            val tickStart = Offset(
                x = center.x + (radius - 12.dp.toPx()) * cos(angle).toFloat(),
                y = center.y + (radius - 12.dp.toPx()) * sin(angle).toFloat()
            )
            val tickEnd = Offset(
                x = center.x + radius * cos(angle).toFloat(),
                y = center.y + radius * sin(angle).toFloat()
            )
            drawLine(
                color = handColor.copy(alpha = if (i % 3 == 0) 0.9f else 0.4f),
                start = tickStart,
                end = tickEnd,
                strokeWidth = if (i % 3 == 0) 3.5.dp.toPx() else 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Hour Hand
        val hourAngle = ((hour + minute / 60f) * 30.0 - 90.0) * (PI / 180.0)
        val hourHandLength = radius * 0.52f
        drawLine(
            color = handColor,
            start = center,
            end = Offset(
                x = center.x + hourHandLength * cos(hourAngle).toFloat(),
                y = center.y + hourHandLength * sin(hourAngle).toFloat()
            ),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minuteAngle = ((minute + second / 60f) * 6.0 - 90.0) * (PI / 180.0)
        val minuteHandLength = radius * 0.78f
        drawLine(
            color = handColor.copy(alpha = 0.9f),
            start = center,
            end = Offset(
                x = center.x + minuteHandLength * cos(minuteAngle).toFloat(),
                y = center.y + minuteHandLength * sin(minuteAngle).toFloat()
            ),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second Hand
        if (showSeconds) {
            val secondAngle = (second * 6.0 - 90.0) * (PI / 180.0)
            val secondHandLength = radius * 0.85f
            drawLine(
                color = secondHandColor,
                start = center,
                end = Offset(
                    x = center.x + secondHandLength * cos(secondAngle).toFloat(),
                    y = center.y + secondHandLength * sin(secondAngle).toFloat()
                ),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center Pivot
        drawCircle(
            color = centerDotColor,
            radius = 6.dp.toPx(),
            center = center
        )
    }
}

@Composable
private fun FlipClock(time: ZonedDateTime, is24Hour: Boolean, showSeconds: Boolean) {
    val hour = if (is24Hour) time.hour else (if (time.hour % 12 == 0) 12 else time.hour % 12)
    val minute = time.minute
    val second = time.second

    val hStr = String.format(java.util.Locale.US, "%02d", hour)
    val mStr = String.format(java.util.Locale.US, "%02d", minute)
    val sStr = String.format(java.util.Locale.US, "%02d", second)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FlipCard(hStr[0].toString())
        FlipCard(hStr[1].toString())
        Text(":", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        FlipCard(mStr[0].toString())
        FlipCard(mStr[1].toString())

        if (showSeconds) {
            Text(":", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            FlipCard(sStr[0].toString(), isSmall = true)
            FlipCard(sStr[1].toString(), isSmall = true)
        }
    }
}

@Composable
private fun FlipCard(digit: String, isSmall: Boolean = false) {
    val width = if (isSmall) 38.dp else 56.dp
    val height = if (isSmall) 58.dp else 84.dp
    val fontSize = if (isSmall) 34.sp else 54.sp

    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Horizontal seam line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .align(Alignment.Center)
        )

        AnimatedContent(
            targetState = digit,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "flip"
        ) { targetDigit ->
            Text(
                text = targetDigit,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MinimalClock(time: ZonedDateTime, is24Hour: Boolean) {
    val pattern = if (is24Hour) "HH:mm" else "h:mm"
    val timeText = time.format(DateTimeFormatter.ofPattern(pattern))

    Text(
        text = timeText,
        fontSize = 88.sp,
        fontWeight = FontWeight.ExtraLight,
        fontFamily = FontFamily.SansSerif,
        color = MaterialTheme.colorScheme.onSurface,
        letterSpacing = (-2).sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BoldClock(time: ZonedDateTime, is24Hour: Boolean) {
    val pattern = if (is24Hour) "HH:mm" else "h:mm"
    val timeText = time.format(DateTimeFormatter.ofPattern(pattern))

    Text(
        text = timeText,
        fontSize = 92.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.SansSerif,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = (-3).sp,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun WordClock(time: ZonedDateTime) {
    val hours = arrayOf(
        "TWELVE", "ONE", "TWO", "THREE", "FOUR", "FIVE",
        "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN"
    )
    val h = hours[time.hour % 12]
    val m = time.minute

    val minuteWords = when (m) {
        0 -> "O'CLOCK"
        in 1..9 -> "OH " + numbersToWord(m)
        15 -> "QUARTER PAST"
        30 -> "HALF PAST"
        45 -> "QUARTER TO"
        else -> numbersToWord(m)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "IT IS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = h,
            fontSize = 54.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = minuteWords,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

private fun numbersToWord(n: Int): String {
    val units = arrayOf("", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE")
    val teens = arrayOf("TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN", "SEVENTEEN", "EIGHTEEN", "NINETEEN")
    val tens = arrayOf("", "", "TWENTY", "THIRTY", "FORTY", "FIFTY")

    return when {
        n < 10 -> units[n]
        n in 10..19 -> teens[n - 10]
        else -> tens[n / 10] + if (n % 10 != 0) " " + units[n % 10] else ""
    }
}

@Composable
private fun BinaryClock(time: ZonedDateTime) {
    val h = time.hour
    val m = time.minute
    val s = time.second

    val columns = listOf(
        h / 10, h % 10,
        m / 10, m % 10,
        s / 10, s % 10
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            columns.forEachIndexed { colIndex, value ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (bit in 3 downTo 0) {
                        val isSet = (value and (1 shl bit)) != 0
                        val activeColor = MaterialTheme.colorScheme.primary
                        val inactiveColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)

                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isSet) activeColor else inactiveColor)
                                .border(
                                    1.dp,
                                    if (isSet) activeColor.copy(alpha = 0.8f) else Color.Transparent,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
        Text(
            text = "BCD BINARY",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        )
    }
}

@Composable
private fun VerticalStackClock(time: ZonedDateTime, is24Hour: Boolean) {
    val hour = if (is24Hour) time.hour else (if (time.hour % 12 == 0) 12 else time.hour % 12)
    val minute = time.minute

    val hStr = String.format(java.util.Locale.US, "%02d", hour)
    val mStr = String.format(java.util.Locale.US, "%02d", minute)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(-20.dp)
    ) {
        Text(
            text = hStr,
            fontSize = 90.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 90.sp
        )
        Text(
            text = mStr,
            fontSize = 90.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            color = MaterialTheme.colorScheme.secondary,
            lineHeight = 90.sp
        )
    }
}

