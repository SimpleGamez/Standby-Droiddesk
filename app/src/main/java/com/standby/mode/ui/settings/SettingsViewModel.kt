package com.standby.mode.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.data.repository.SettingsRepository
import com.standby.mode.domain.model.BrightnessConfig
import com.standby.mode.domain.model.BurnInConfig
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ColorPalette
import com.standby.mode.domain.model.NightMode
import com.standby.mode.domain.model.StandbyProfile
import com.standby.mode.domain.model.StandbySettings
import com.standby.mode.domain.model.ThemeConfig
import com.standby.mode.domain.model.TriggerConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@Serializable
data class StandbyBackup(
    val version: Int = 1,
    val settings: StandbySettings,
    val profiles: List<StandbyProfile>
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val activeProfile: StateFlow<StandbyProfile?> = profileRepository.getActiveProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProfiles: StateFlow<List<StandbyProfile>> = profileRepository.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<StandbySettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StandbySettings())

    private val _backupJson = MutableStateFlow<String?>(null)
    val backupJson: StateFlow<String?> = _backupJson.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    fun updateClockStyle(style: ClockStyle) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(clockStyle = style) }
        }
    }

    fun update24Hour(is24Hour: Boolean) {
        viewModelScope.launch {
            settingsRepository.set24Hour(is24Hour)
        }
    }

    fun updateShowSeconds(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowSeconds(show)
        }
    }

    fun updateDateFormat(format: String) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(dateFormat = format) }
        }
    }

    fun updateAutoRotate(enabled: Boolean, interval: Int) {
        viewModelScope.launch {
            settingsRepository.setPageAutoRotate(enabled, interval)
        }
    }

    fun updateColorPalette(palette: ColorPalette) {
        val profile = activeProfile.value ?: return
        val updatedTheme = profile.themeConfig.copy(colorPalette = palette)
        updateProfile(profile.copy(themeConfig = updatedTheme))
    }

    fun updateGradientBackground(enabled: Boolean) {
        val profile = activeProfile.value ?: return
        val updatedTheme = profile.themeConfig.copy(
            useGradientBackground = enabled,
            gradientColors = if (enabled) listOf(0xFF1A1A2E, 0xFF16213E, 0xFF0F3460) else emptyList()
        )
        updateProfile(profile.copy(themeConfig = updatedTheme))
    }

    fun updateTriggerConfig(transform: (TriggerConfig) -> TriggerConfig) {
        val profile = activeProfile.value ?: return
        val updated = profile.copy(triggerConfig = transform(profile.triggerConfig))
        updateProfile(updated)
    }

    fun updateBurnInConfig(transform: (BurnInConfig) -> BurnInConfig) {
        val profile = activeProfile.value ?: return
        val updated = profile.copy(burnInConfig = transform(profile.burnInConfig))
        updateProfile(updated)
    }

    fun updateNightMode(mode: NightMode) {
        val profile = activeProfile.value ?: return
        val updated = profile.copy(
            brightnessConfig = profile.brightnessConfig.copy(nightMode = mode)
        )
        updateProfile(updated)
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            profileRepository.setActiveProfile(profileId)
            _message.value = "Active profile switched"
        }
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            val newProfile = StandbyProfile(
                id = "profile_${UUID.randomUUID()}",
                name = name,
                pages = profileRepository.createDefaultDeskProfile().pages,
                isActive = false
            )
            profileRepository.saveProfile(newProfile)
            _message.value = "Created profile '$name'"
        }
    }

    private fun updateProfile(profile: StandbyProfile) {
        viewModelScope.launch {
            profileRepository.saveProfile(profile)
        }
    }

    fun generateBackupJson() {
        viewModelScope.launch {
            val currentSettings = settings.value
            val currentProfiles = allProfiles.value
            val backup = StandbyBackup(
                version = 1,
                settings = currentSettings,
                profiles = currentProfiles
            )
            _backupJson.value = json.encodeToString(backup)
        }
    }

    fun restoreFromJson(backupString: String): Boolean {
        return try {
            val backup = json.decodeFromString<StandbyBackup>(backupString)
            viewModelScope.launch {
                settingsRepository.updateSettings { backup.settings }
                backup.profiles.forEach { profile ->
                    profileRepository.saveProfile(profile)
                }
                _message.value = "Backup restored successfully"
            }
            true
        } catch (e: Exception) {
            _message.value = "Failed to parse backup JSON: ${e.localizedMessage}"
            false
        }
    }
}

