package com.standby.mode.ui.modules

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standby.mode.domain.model.HourlyWeather
import com.standby.mode.domain.model.TemperatureUnit
import com.standby.mode.domain.model.WeatherCondition
import com.standby.mode.domain.model.WeatherData
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeatherModule(
    weather: WeatherData?,
    isLoading: Boolean = false,
    unit: TemperatureUnit = TemperatureUnit.CELSIUS,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading && weather == null) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
            return@Box
        }

        if (weather == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🌤️",
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Weather Unavailable",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Box
        }

        fun formatTemp(celsius: Double): String {
            val value = if (unit == TemperatureUnit.FAHRENHEIT) {
                (celsius * 9 / 5) + 32
            } else celsius
            return "${value.roundToInt()}°"
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = getWeatherEmoji(weather.condition),
                    fontSize = 52.sp
                )

                Column {
                    Text(
                        text = formatTemp(weather.temperature),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = weather.condition.name.replace("_", " "),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // High / Low & Details
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "H: ${formatTemp(weather.highTemp)}  L: ${formatTemp(weather.lowTemp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "💧 ${weather.humidity}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.US, "💨 %.0f km/h", weather.windSpeed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location
            if (weather.locationName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${weather.locationName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                )
            }

            // Hourly forecast strip if available
            if (weather.hourlyForecast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    items(weather.hourlyForecast.take(6)) { hourly ->
                        HourlyWeatherItem(hourly = hourly, unit = unit)
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyWeatherItem(hourly: HourlyWeather, unit: TemperatureUnit) {
    val tempValue = if (unit == TemperatureUnit.FAHRENHEIT) {
        (hourly.temperature * 9 / 5) + 32
    } else hourly.temperature

    val timeStr = androidx.compose.runtime.remember(hourly.time) {
        try {
            java.time.Instant.ofEpochMilli(hourly.time)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("h a"))
        } catch (e: Exception) {
            "--"
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = getWeatherEmoji(hourly.condition),
                fontSize = 16.sp
            )
            Text(
                text = "${tempValue.roundToInt()}°",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

private fun getWeatherEmoji(condition: WeatherCondition): String {
    return when (condition) {
        WeatherCondition.CLEAR -> "☀️"
        WeatherCondition.PARTLY_CLOUDY -> "⛅"
        WeatherCondition.CLOUDY -> "☁️"
        WeatherCondition.RAIN -> "🌧️"
        WeatherCondition.DRIZZLE -> "🌦️"
        WeatherCondition.SNOW -> "❄️"
        WeatherCondition.THUNDERSTORM -> "⛈️"
        WeatherCondition.FOG -> "🌫️"
    }
}

