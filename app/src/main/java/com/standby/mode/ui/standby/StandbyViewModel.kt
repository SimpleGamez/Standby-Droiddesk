package com.standby.mode.ui.standby

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standby.mode.data.repository.CalendarRepository
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.data.repository.SettingsRepository
import com.standby.mode.data.repository.WeatherRepository
import com.standby.mode.domain.model.BatteryInfo
import com.standby.mode.domain.model.CalendarEvent
import com.standby.mode.domain.model.GestureAction
import com.standby.mode.domain.model.MediaInfo
import com.standby.mode.domain.model.NotificationItem
import com.standby.mode.domain.model.StandbyProfile
import com.standby.mode.domain.model.StandbySettings
import com.standby.mode.domain.model.WeatherData
import com.standby.mode.domain.provider.BatteryProvider
import com.standby.mode.domain.provider.MediaSessionProvider
import com.standby.mode.service.BrightnessController
import com.standby.mode.service.StandbyNotificationListenerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StandbyViewModel @Inject constructor(
    application: Application,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val weatherRepository: WeatherRepository,
    private val calendarRepository: CalendarRepository,
    private val batteryProvider: BatteryProvider,
    private val mediaSessionProvider: MediaSessionProvider,
    val brightnessController: BrightnessController
) : AndroidViewModel(application) {

    val activeProfile: StateFlow<StandbyProfile?> = profileRepository.getActiveProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<StandbySettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StandbySettings())

    val batteryInfo: StateFlow<BatteryInfo> = batteryProvider.getBatteryInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), batteryProvider.getCurrentBatteryInfo())

    val mediaInfo: StateFlow<MediaInfo?> = mediaSessionProvider.getActiveMediaInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentWeather: StateFlow<WeatherData?> = weatherRepository.currentWeather
    val isWeatherLoading: StateFlow<Boolean> = weatherRepository.isLoading

    val calendarEvents: StateFlow<List<CalendarEvent>> = calendarRepository.eventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationItem>> = StandbyNotificationListenerService.activeNotifications

    private val _isControlsVisible = MutableStateFlow(false)
    val isControlsVisible: StateFlow<Boolean> = _isControlsVisible.asStateFlow()

    private val _isDimmed = MutableStateFlow(false)
    val isDimmed: StateFlow<Boolean> = _isDimmed.asStateFlow()

    private var hideControlsJob: Job? = null
    private var autoDimJob: Job? = null

    init {
        viewModelScope.launch {
            profileRepository.ensureDefaultProfilesExist()
            calendarRepository.refreshEvents()
            brightnessController.startListening()

            // Fetch initial weather if location is configured or use default coordinates (e.g. San Francisco or London)
            settings.collect { set ->
                val lat = set.weatherLocationLat ?: 37.7749
                val lon = set.weatherLocationLon ?: -122.4194
                weatherRepository.refreshWeather(lat, lon)
            }
        }
        resetDimTimer()
    }

    fun onUserInteraction() {
        if (_isDimmed.value) {
            _isDimmed.value = false
        }
        resetDimTimer()
    }

    fun toggleControls() {
        onUserInteraction()
        _isControlsVisible.value = !_isControlsVisible.value
        if (_isControlsVisible.value) {
            hideControlsJob?.cancel()
            hideControlsJob = viewModelScope.launch {
                delay(4000L)
                _isControlsVisible.value = false
            }
        }
    }

    fun hideControls() {
        _isControlsVisible.value = false
        hideControlsJob?.cancel()
    }

    fun toggleDim() {
        _isDimmed.value = !_isDimmed.value
    }

    private fun resetDimTimer() {
        autoDimJob?.cancel()
        val burnInConfig = activeProfile.value?.burnInConfig
        if (burnInConfig?.autoDimEnabled == true) {
            autoDimJob = viewModelScope.launch {
                val delayMs = (burnInConfig.autoDimAfterMinutes * 60 * 1000L).coerceAtLeast(15000L)
                delay(delayMs)
                _isDimmed.value = true
            }
        }
    }

    fun handleGesture(action: GestureAction, onNextPage: () -> Unit, onPrevPage: () -> Unit, onExit: () -> Unit) {
        onUserInteraction()
        when (action) {
            GestureAction.NEXT_PAGE -> onNextPage()
            GestureAction.PREVIOUS_PAGE -> onPrevPage()
            GestureAction.SHOW_CONTROLS -> toggleControls()
            GestureAction.TOGGLE_DIM -> toggleDim()
            GestureAction.EXIT_STANDBY -> onExit()
            GestureAction.OPEN_SETTINGS, GestureAction.NONE -> Unit
        }
    }

    fun playPauseMedia() = mediaSessionProvider.sendPlayPause()
    fun nextMedia() = mediaSessionProvider.sendSkipNext()
    fun prevMedia() = mediaSessionProvider.sendSkipPrevious()

    override fun onCleared() {
        super.onCleared()
        brightnessController.stopListening()
    }
}

