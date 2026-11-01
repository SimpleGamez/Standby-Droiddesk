package com.standby.mode.data.remote

import com.standby.mode.domain.model.HourlyWeather
import com.standby.mode.domain.model.WeatherCondition
import com.standby.mode.domain.model.WeatherData
import com.standby.mode.domain.provider.WeatherProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenMeteoApi @Inject constructor(
    private val httpClient: HttpClient
) : WeatherProvider {

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherData> {
        return runCatching {
            val response: String = httpClient.get(BASE_URL) {
                parameter("latitude", lat)
                parameter("longitude", lon)
                parameter("current_weather", true)
                parameter("hourly", "temperature_2m,weathercode,relativehumidity_2m,apparent_temperature,windspeed_10m")
                parameter("daily", "temperature_2m_max,temperature_2m_min")
                parameter("timezone", "auto")
                parameter("forecast_days", 1)
            }.body()

            parseWeatherResponse(response, lat, lon)
        }
    }

    private fun parseWeatherResponse(responseBody: String, lat: Double, lon: Double): WeatherData {
        val root = json.decodeFromString<JsonObject>(responseBody)

        val currentWeather = root["current_weather"]!!.jsonObject
        val temperature = currentWeather["temperature"]!!.jsonPrimitive.double
        val windSpeed = currentWeather["windspeed"]!!.jsonPrimitive.double
        val weatherCode = currentWeather["weathercode"]!!.jsonPrimitive.int

        val hourly = root["hourly"]?.jsonObject
        val hourlyTemps = hourly?.get("temperature_2m")?.jsonArray
        val hourlyWeatherCodes = hourly?.get("weathercode")?.jsonArray
        val hourlyTimes = hourly?.get("time")?.jsonArray
        val hourlyHumidity = hourly?.get("relativehumidity_2m")?.jsonArray
        val hourlyApparentTemp = hourly?.get("apparent_temperature")?.jsonArray

        val daily = root["daily"]?.jsonObject
        val dailyMax = daily?.get("temperature_2m_max")?.jsonArray
        val dailyMin = daily?.get("temperature_2m_min")?.jsonArray

        val highTemp = dailyMax?.firstOrNull()?.jsonPrimitive?.doubleOrNull ?: temperature
        val lowTemp = dailyMin?.firstOrNull()?.jsonPrimitive?.doubleOrNull ?: temperature

        // Get current hour's humidity and feels-like from hourly data
        val currentHumidity = hourlyHumidity?.firstOrNull()?.jsonPrimitive?.intOrNull ?: 0
        val currentFeelsLike = hourlyApparentTemp?.firstOrNull()?.jsonPrimitive?.doubleOrNull ?: temperature

        // Parse hourly forecast
        val hourlyForecast = mutableListOf<HourlyWeather>()
        if (hourlyTemps != null && hourlyWeatherCodes != null && hourlyTimes != null) {
            val count = minOf(hourlyTemps.size, hourlyWeatherCodes.size, hourlyTimes.size, 24)
            for (i in 0 until count) {
                val temp = hourlyTemps[i].jsonPrimitive.doubleOrNull ?: continue
                val code = hourlyWeatherCodes[i].jsonPrimitive.intOrNull ?: continue
                val timeStr = hourlyTimes[i].jsonPrimitive.content

                hourlyForecast.add(
                    HourlyWeather(
                        temperature = temp,
                        condition = mapWmoCodeToCondition(code),
                        time = parseIsoTimeToEpoch(timeStr)
                    )
                )
            }
        }

        return WeatherData(
            temperature = temperature,
            feelsLike = currentFeelsLike,
            humidity = currentHumidity,
            windSpeed = windSpeed,
            condition = mapWmoCodeToCondition(weatherCode),
            highTemp = highTemp,
            lowTemp = lowTemp,
            locationName = "%.2f, %.2f".format(lat, lon),
            updatedAt = System.currentTimeMillis(),
            hourlyForecast = hourlyForecast
        )
    }

    private fun parseIsoTimeToEpoch(isoTime: String): Long {
        return try {
            java.time.LocalDateTime.parse(isoTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }

    private fun mapWmoCodeToCondition(code: Int): WeatherCondition {
        return when (code) {
            0 -> WeatherCondition.CLEAR
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOG
            51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.CLEAR
        }
    }
}
