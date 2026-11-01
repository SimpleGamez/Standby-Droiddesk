package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HourlyWeather(
    val temperature: Double,
    val condition: WeatherCondition,
    val time: Long
)

@Serializable
data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val highTemp: Double,
    val lowTemp: Double,
    val locationName: String,
    val updatedAt: Long,
    val hourlyForecast: List<HourlyWeather> = emptyList()
)
