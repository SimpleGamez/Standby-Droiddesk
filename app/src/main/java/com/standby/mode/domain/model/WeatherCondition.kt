package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WeatherCondition {
    CLEAR, PARTLY_CLOUDY, CLOUDY, RAIN, SNOW, THUNDERSTORM, FOG, DRIZZLE
}
