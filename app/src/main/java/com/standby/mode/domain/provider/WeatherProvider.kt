package com.standby.mode.domain.provider

import com.standby.mode.domain.model.WeatherData

interface WeatherProvider {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherData>
}
