package com.standby.mode.data.repository

import com.standby.mode.data.local.SettingsDataStore
import com.standby.mode.domain.model.StandbySettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface SettingsRepository {
    val settingsFlow: Flow<StandbySettings>
    suspend fun updateSettings(transform: (StandbySettings) -> StandbySettings)
    suspend fun setActiveProfile(profileId: String)
    suspend fun set24Hour(is24Hour: Boolean)
    suspend fun setShowSeconds(showSeconds: Boolean)
    suspend fun setPageAutoRotate(enabled: Boolean, intervalSeconds: Int)
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val settingsFlow: Flow<StandbySettings> = dataStore.settingsFlow

    override suspend fun updateSettings(transform: (StandbySettings) -> StandbySettings) {
        dataStore.updateSettings(transform)
    }

    override suspend fun setActiveProfile(profileId: String) {
        dataStore.updateSettings { it.copy(activeProfileId = profileId) }
    }

    override suspend fun set24Hour(is24Hour: Boolean) {
        dataStore.updateSettings { it.copy(is24Hour = is24Hour) }
    }

    override suspend fun setShowSeconds(showSeconds: Boolean) {
        dataStore.updateSettings { it.copy(showSeconds = showSeconds) }
    }

    override suspend fun setPageAutoRotate(enabled: Boolean, intervalSeconds: Int) {
        dataStore.updateSettings {
            it.copy(
                pageAutoRotateEnabled = enabled,
                pageAutoRotateIntervalSeconds = intervalSeconds
            )
        }
    }
}

