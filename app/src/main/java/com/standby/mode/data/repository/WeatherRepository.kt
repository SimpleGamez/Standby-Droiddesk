package com.standby.mode.data.repository

import com.standby.mode.domain.model.WeatherData
import com.standby.mode.domain.provider.WeatherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface WeatherRepository {
    val currentWeather: StateFlow<WeatherData?>
    val isLoading: StateFlow<Boolean>
    suspend fun refreshWeather(lat: Double, lon: Double): Result<WeatherData>
}

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherProvider: WeatherProvider
) : WeatherRepository {

    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    override val currentWeather: StateFlow<WeatherData?> = _currentWeather.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override suspend fun refreshWeather(lat: Double, lon: Double): Result<WeatherData> {
        _isLoading.value = true
        val result = weatherProvider.getWeather(lat, lon)
        _isLoading.value = false
        result.onSuccess { data ->
            _currentWeather.value = data
        }
        return result
    }
}

