package com.standby.mode.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.GestureAction
import com.standby.mode.domain.model.StandbySettings
import com.standby.mode.domain.model.TemperatureUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "standby_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val ADDITIONAL_TIME_ZONES = stringPreferencesKey("additional_time_zones")
        val PAGE_AUTO_ROTATE_ENABLED = booleanPreferencesKey("page_auto_rotate_enabled")
        val PAGE_AUTO_ROTATE_INTERVAL = intPreferencesKey("page_auto_rotate_interval")
        val TAP_ACTION = stringPreferencesKey("tap_action")
        val SWIPE_LEFT_ACTION = stringPreferencesKey("swipe_left_action")
        val SWIPE_RIGHT_ACTION = stringPreferencesKey("swipe_right_action")
        val SWIPE_UP_ACTION = stringPreferencesKey("swipe_up_action")
        val EXIT_ON_UNPLUG = booleanPreferencesKey("exit_on_unplug")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SHOW_ON_LOCK_SCREEN = booleanPreferencesKey("show_on_lock_screen")
        val PHOTO_FOLDER_URI = stringPreferencesKey("photo_folder_uri")
        val PHOTO_INTERVAL_SECONDS = intPreferencesKey("photo_interval_seconds")
        val KEN_BURNS_ENABLED = booleanPreferencesKey("ken_burns_enabled")
        val NOTIFICATION_PRIVACY_MODE = booleanPreferencesKey("notification_privacy_mode")
        val WEATHER_LOCATION_LAT = doublePreferencesKey("weather_location_lat")
        val WEATHER_LOCATION_LON = doublePreferencesKey("weather_location_lon")
        val WEATHER_LOCATION_NAME = stringPreferencesKey("weather_location_name")
        val WEATHER_UPDATE_INTERVAL = intPreferencesKey("weather_update_interval")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }

    val settingsFlow: Flow<StandbySettings> = context.dataStore.data.map { prefs ->
        val defaults = StandbySettings()
        StandbySettings(
            activeProfileId = prefs[Keys.ACTIVE_PROFILE_ID] ?: defaults.activeProfileId,
            is24Hour = prefs[Keys.IS_24_HOUR] ?: defaults.is24Hour,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: defaults.showSeconds,
            clockStyle = prefs[Keys.CLOCK_STYLE]?.let { runCatching { ClockStyle.valueOf(it) }.getOrNull() } ?: defaults.clockStyle,
            dateFormat = prefs[Keys.DATE_FORMAT] ?: defaults.dateFormat,
            additionalTimeZones = prefs[Keys.ADDITIONAL_TIME_ZONES]?.let {
                runCatching { json.decodeFromString<List<String>>(it) }.getOrNull()
            } ?: defaults.additionalTimeZones,
            pageAutoRotateEnabled = prefs[Keys.PAGE_AUTO_ROTATE_ENABLED] ?: defaults.pageAutoRotateEnabled,
            pageAutoRotateIntervalSeconds = prefs[Keys.PAGE_AUTO_ROTATE_INTERVAL] ?: defaults.pageAutoRotateIntervalSeconds,
            tapAction = prefs[Keys.TAP_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.tapAction,
            swipeLeftAction = prefs[Keys.SWIPE_LEFT_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.swipeLeftAction,
            swipeRightAction = prefs[Keys.SWIPE_RIGHT_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.swipeRightAction,
            swipeUpAction = prefs[Keys.SWIPE_UP_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.swipeUpAction,
            exitOnUnplug = prefs[Keys.EXIT_ON_UNPLUG] ?: defaults.exitOnUnplug,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
            showOnLockScreen = prefs[Keys.SHOW_ON_LOCK_SCREEN] ?: defaults.showOnLockScreen,
            photoFolderUri = prefs[Keys.PHOTO_FOLDER_URI],
            photoIntervalSeconds = prefs[Keys.PHOTO_INTERVAL_SECONDS] ?: defaults.photoIntervalSeconds,
            kenBurnsEnabled = prefs[Keys.KEN_BURNS_ENABLED] ?: defaults.kenBurnsEnabled,
            notificationPrivacyMode = prefs[Keys.NOTIFICATION_PRIVACY_MODE] ?: defaults.notificationPrivacyMode,
            weatherLocationLat = prefs[Keys.WEATHER_LOCATION_LAT],
            weatherLocationLon = prefs[Keys.WEATHER_LOCATION_LON],
            weatherLocationName = prefs[Keys.WEATHER_LOCATION_NAME],
            weatherUpdateIntervalMinutes = prefs[Keys.WEATHER_UPDATE_INTERVAL] ?: defaults.weatherUpdateIntervalMinutes,
            temperatureUnit = prefs[Keys.TEMPERATURE_UNIT]?.let { runCatching { TemperatureUnit.valueOf(it) }.getOrNull() } ?: defaults.temperatureUnit
        )
    }

    suspend fun updateSettings(transform: (StandbySettings) -> StandbySettings) {
        context.dataStore.edit { prefs ->
            val defaults = StandbySettings()
            val current = StandbySettings(
                activeProfileId = prefs[Keys.ACTIVE_PROFILE_ID] ?: defaults.activeProfileId,
                is24Hour = prefs[Keys.IS_24_HOUR] ?: defaults.is24Hour,
                showSeconds = prefs[Keys.SHOW_SECONDS] ?: defaults.showSeconds,
                clockStyle = prefs[Keys.CLOCK_STYLE]?.let { runCatching { ClockStyle.valueOf(it) }.getOrNull() } ?: defaults.clockStyle,
                dateFormat = prefs[Keys.DATE_FORMAT] ?: defaults.dateFormat,
                additionalTimeZones = prefs[Keys.ADDITIONAL_TIME_ZONES]?.let {
                    runCatching { json.decodeFromString<List<String>>(it) }.getOrNull()
                } ?: defaults.additionalTimeZones,
                pageAutoRotateEnabled = prefs[Keys.PAGE_AUTO_ROTATE_ENABLED] ?: defaults.pageAutoRotateEnabled,
                pageAutoRotateIntervalSeconds = prefs[Keys.PAGE_AUTO_ROTATE_INTERVAL] ?: defaults.pageAutoRotateIntervalSeconds,
                tapAction = prefs[Keys.TAP_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.tapAction,
                swipeLeftAction = prefs[Keys.SWIPE_LEFT_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.swipeLeftAction,
                swipeRightAction = prefs[Keys.SWIPE_RIGHT_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.swipeRightAction,
                swipeUpAction = prefs[Keys.SWIPE_UP_ACTION]?.let { runCatching { GestureAction.valueOf(it) }.getOrNull() } ?: defaults.swipeUpAction,
                exitOnUnplug = prefs[Keys.EXIT_ON_UNPLUG] ?: defaults.exitOnUnplug,
                keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
                showOnLockScreen = prefs[Keys.SHOW_ON_LOCK_SCREEN] ?: defaults.showOnLockScreen,
                photoFolderUri = prefs[Keys.PHOTO_FOLDER_URI],
                photoIntervalSeconds = prefs[Keys.PHOTO_INTERVAL_SECONDS] ?: defaults.photoIntervalSeconds,
                kenBurnsEnabled = prefs[Keys.KEN_BURNS_ENABLED] ?: defaults.kenBurnsEnabled,
                notificationPrivacyMode = prefs[Keys.NOTIFICATION_PRIVACY_MODE] ?: defaults.notificationPrivacyMode,
                weatherLocationLat = prefs[Keys.WEATHER_LOCATION_LAT],
                weatherLocationLon = prefs[Keys.WEATHER_LOCATION_LON],
                weatherLocationName = prefs[Keys.WEATHER_LOCATION_NAME],
                weatherUpdateIntervalMinutes = prefs[Keys.WEATHER_UPDATE_INTERVAL] ?: defaults.weatherUpdateIntervalMinutes,
                temperatureUnit = prefs[Keys.TEMPERATURE_UNIT]?.let { runCatching { TemperatureUnit.valueOf(it) }.getOrNull() } ?: defaults.temperatureUnit
            )
            val updated = transform(current)
            writeSettingsToPrefs(prefs, updated)
        }
    }

    suspend fun updateClockStyle(style: ClockStyle) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CLOCK_STYLE] = style.name
        }
    }

    suspend fun updateIs24Hour(is24Hour: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_24_HOUR] = is24Hour
        }
    }

    suspend fun updateShowSeconds(showSeconds: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SHOW_SECONDS] = showSeconds
        }
    }

    suspend fun updateActiveProfileId(profileId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_PROFILE_ID] = profileId
        }
    }

    suspend fun updateKeepScreenOn(keepScreenOn: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.KEEP_SCREEN_ON] = keepScreenOn
        }
    }

    suspend fun updateExitOnUnplug(exitOnUnplug: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EXIT_ON_UNPLUG] = exitOnUnplug
        }
    }

    suspend fun updateWeatherLocation(lat: Double, lon: Double, name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEATHER_LOCATION_LAT] = lat
            prefs[Keys.WEATHER_LOCATION_LON] = lon
            prefs[Keys.WEATHER_LOCATION_NAME] = name
        }
    }

    suspend fun updateTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TEMPERATURE_UNIT] = unit.name
        }
    }

    suspend fun updateNotificationPrivacyMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_PRIVACY_MODE] = enabled
        }
    }

    suspend fun updatePhotoSettings(folderUri: String?, intervalSeconds: Int, kenBurnsEnabled: Boolean) {
        context.dataStore.edit { prefs ->
            if (folderUri != null) {
                prefs[Keys.PHOTO_FOLDER_URI] = folderUri
            } else {
                prefs.remove(Keys.PHOTO_FOLDER_URI)
            }
            prefs[Keys.PHOTO_INTERVAL_SECONDS] = intervalSeconds
            prefs[Keys.KEN_BURNS_ENABLED] = kenBurnsEnabled
        }
    }

    suspend fun updateGestureActions(
        tapAction: GestureAction,
        swipeLeftAction: GestureAction,
        swipeRightAction: GestureAction,
        swipeUpAction: GestureAction
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TAP_ACTION] = tapAction.name
            prefs[Keys.SWIPE_LEFT_ACTION] = swipeLeftAction.name
            prefs[Keys.SWIPE_RIGHT_ACTION] = swipeRightAction.name
            prefs[Keys.SWIPE_UP_ACTION] = swipeUpAction.name
        }
    }

    suspend fun updatePageAutoRotate(enabled: Boolean, intervalSeconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PAGE_AUTO_ROTATE_ENABLED] = enabled
            prefs[Keys.PAGE_AUTO_ROTATE_INTERVAL] = intervalSeconds
        }
    }

    private fun writeSettingsToPrefs(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        settings: StandbySettings
    ) {
        prefs[Keys.ACTIVE_PROFILE_ID] = settings.activeProfileId
        prefs[Keys.IS_24_HOUR] = settings.is24Hour
        prefs[Keys.SHOW_SECONDS] = settings.showSeconds
        prefs[Keys.CLOCK_STYLE] = settings.clockStyle.name
        prefs[Keys.DATE_FORMAT] = settings.dateFormat
        prefs[Keys.ADDITIONAL_TIME_ZONES] = json.encodeToString(settings.additionalTimeZones)
        prefs[Keys.PAGE_AUTO_ROTATE_ENABLED] = settings.pageAutoRotateEnabled
        prefs[Keys.PAGE_AUTO_ROTATE_INTERVAL] = settings.pageAutoRotateIntervalSeconds
        prefs[Keys.TAP_ACTION] = settings.tapAction.name
        prefs[Keys.SWIPE_LEFT_ACTION] = settings.swipeLeftAction.name
        prefs[Keys.SWIPE_RIGHT_ACTION] = settings.swipeRightAction.name
        prefs[Keys.SWIPE_UP_ACTION] = settings.swipeUpAction.name
        prefs[Keys.EXIT_ON_UNPLUG] = settings.exitOnUnplug
        prefs[Keys.KEEP_SCREEN_ON] = settings.keepScreenOn
        prefs[Keys.SHOW_ON_LOCK_SCREEN] = settings.showOnLockScreen
        if (settings.photoFolderUri != null) {
            prefs[Keys.PHOTO_FOLDER_URI] = settings.photoFolderUri
        } else {
            prefs.remove(Keys.PHOTO_FOLDER_URI)
        }
        prefs[Keys.PHOTO_INTERVAL_SECONDS] = settings.photoIntervalSeconds
        prefs[Keys.KEN_BURNS_ENABLED] = settings.kenBurnsEnabled
        prefs[Keys.NOTIFICATION_PRIVACY_MODE] = settings.notificationPrivacyMode
        if (settings.weatherLocationLat != null) {
            prefs[Keys.WEATHER_LOCATION_LAT] = settings.weatherLocationLat
        } else {
            prefs.remove(Keys.WEATHER_LOCATION_LAT)
        }
        if (settings.weatherLocationLon != null) {
            prefs[Keys.WEATHER_LOCATION_LON] = settings.weatherLocationLon
        } else {
            prefs.remove(Keys.WEATHER_LOCATION_LON)
        }
        if (settings.weatherLocationName != null) {
            prefs[Keys.WEATHER_LOCATION_NAME] = settings.weatherLocationName
        } else {
            prefs.remove(Keys.WEATHER_LOCATION_NAME)
        }
        prefs[Keys.WEATHER_UPDATE_INTERVAL] = settings.weatherUpdateIntervalMinutes
        prefs[Keys.TEMPERATURE_UNIT] = settings.temperatureUnit.name
    }
}
