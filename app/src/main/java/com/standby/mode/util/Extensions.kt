package com.standby.mode.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Launch StandbyActivity from any context.
 */
fun Context.startStandbyActivity() {
    val intent = Intent().apply {
        setClassName(packageName, "com.standby.mode.ui.standby.StandbyActivity")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
    startActivity(intent)
}

/**
 * Returns `true` when the device is currently connected to a charger.
 */
fun Context.isCharging(): Boolean {
    val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
}

/**
 * Returns the current battery level as an integer percentage (0–100).
 */
fun Context.batteryLevel(): Int {
    val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    return if (level >= 0 && scale > 0) (level * 100) / scale else 0
}

/**
 * Enables fullscreen immersive mode, hiding both system bars.
 */
fun Window.setFullScreenImmersive() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }
}

/**
 * Formats a duration in milliseconds to a human-readable string.
 * Examples: "2h 15m", "45m", "0m".
 */
fun Long.formatDuration(): String {
    if (this <= 0L) return "0m"
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this) % 60
    return buildString {
        if (hours > 0) append("${hours}h ")
        append("${minutes}m")
    }.trim()
}

/**
 * Celsius ↔ Fahrenheit conversions.
 */
fun Float.toCelsius(): Float = (this - 32f) * 5f / 9f
fun Float.toFahrenheit(): Float = this * 9f / 5f + 32f

/**
 * Returns `true` if the app has been granted Notification Listener permission.
 */
fun Context.hasNotificationListenerPermission(): Boolean {
    val componentName = ComponentName(this, "com.standby.mode.service.StandbyNotificationListenerService")
    val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return enabledListeners?.contains(componentName.flattenToString()) == true
}
