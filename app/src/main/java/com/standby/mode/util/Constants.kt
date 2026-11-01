package com.standby.mode.util

/**
 * Application-wide constants for StandBy Mode.
 */
object Constants {

    // ── Notification Channels ───────────────────────────────────────────
    const val NOTIFICATION_CHANNEL_ID = "standby_channel"
    const val NOTIFICATION_CHANNEL_NAME = "StandBy Mode"
    const val NOTIFICATION_ID = 1001

    // ── DataStore ───────────────────────────────────────────────────────
    const val DATASTORE_NAME = "standby_preferences"
    const val SETTINGS_KEY = "standby_settings"
    const val TRIGGER_KEY = "trigger_config"
    const val BRIGHTNESS_KEY = "brightness_config"

    // ── Request Codes ───────────────────────────────────────────────────
    const val REQUEST_CODE_STOP = 2001
    const val REQUEST_CODE_SETTINGS = 2002
    const val REQUEST_CODE_FULLSCREEN = 2003

    // ── Custom Intent Actions ───────────────────────────────────────────
    const val ACTION_STOP_STANDBY = "com.standby.mode.ACTION_STOP_STANDBY"
    const val ACTION_START_STANDBY = "com.standby.mode.ACTION_START_STANDBY"
    const val ACTION_TOGGLE_STANDBY = "com.standby.mode.ACTION_TOGGLE_STANDBY"
    const val ACTION_OPEN_SETTINGS = "com.standby.mode.ACTION_OPEN_SETTINGS"

    // ── Foreground Service Commands ─────────────────────────────────────
    const val COMMAND_START = "COMMAND_START"
    const val COMMAND_STOP = "COMMAND_STOP"
    const val EXTRA_COMMAND = "extra_command"

    // ── Sensor Defaults ─────────────────────────────────────────────────
    const val STATIONARY_THRESHOLD = 0.3f
    const val STATIONARY_SAMPLE_COUNT = 20
    const val LANDSCAPE_THRESHOLD_DEGREES = 45

    // ── Wake Lock Tag ───────────────────────────────────────────────────
    const val WAKE_LOCK_TAG = "standby:foreground_wake_lock"

    // ── Calendar ────────────────────────────────────────────────────────
    const val CALENDAR_LOOKAHEAD_MILLIS = 24L * 60L * 60L * 1000L // 24 hours
}
