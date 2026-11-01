package com.standby.mode.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Window
import android.view.WindowManager
import com.standby.mode.domain.model.BrightnessConfig
import com.standby.mode.domain.model.BrightnessMode
import com.standby.mode.domain.model.NightMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrightnessController @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _ambientLux = MutableStateFlow(100f)
    val ambientLux: StateFlow<Float> = _ambientLux.asStateFlow()

    private val _effectiveBrightness = MutableStateFlow(0.5f)
    val effectiveBrightness: StateFlow<Float> = _effectiveBrightness.asStateFlow()

    private val _isNightActive = MutableStateFlow(false)
    val isNightActive: StateFlow<Boolean> = _isNightActive.asStateFlow()

    private var isListening = false

    fun startListening() {
        if (!isListening && lightSensor != null) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager?.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            _ambientLux.value = lux
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun applyBrightness(window: Window, config: BrightnessConfig) {
        val targetBrightness = calculateTargetBrightness(config)
        _effectiveBrightness.value = targetBrightness
        _isNightActive.value = isNightTime(config.nightStartTime, config.nightEndTime, config.nightMode)

        val params = window.attributes
        params.screenBrightness = targetBrightness.coerceIn(0.01f, 1.0f)
        window.attributes = params
    }

    fun calculateTargetBrightness(config: BrightnessConfig): Float {
        return when (config.mode) {
            BrightnessMode.MANUAL -> config.manualLevel
            BrightnessMode.AUTO_SENSOR -> {
                // Map lux: 0 lux -> 0.05f, 500+ lux -> 1.0f
                val lux = _ambientLux.value
                val mapped = (lux / 400f).coerceIn(0.05f, 1.0f)
                mapped
            }
            BrightnessMode.SCHEDULED -> {
                val now = LocalTime.now()
                val isDimPeriod = isTimeBetween(now, config.scheduledDimTime, config.scheduledBrightTime)
                if (isDimPeriod) config.dimLevel else config.manualLevel
            }
        }
    }

    fun isNightTime(startTime: String, endTime: String, mode: NightMode): Boolean {
        if (mode == NightMode.OFF) return false
        if (mode == NightMode.AUTO) {
            // Auto checks if ambient light is very low (< 5 lux)
            return _ambientLux.value < 5f
        }
        val now = LocalTime.now()
        return isTimeBetween(now, startTime, endTime)
    }

    private fun isTimeBetween(now: LocalTime, startStr: String, endStr: String): Boolean {
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val start = LocalTime.parse(startStr, formatter)
            val end = LocalTime.parse(endStr, formatter)
            if (start.isBefore(end)) {
                now.isAfter(start) && now.isBefore(end)
            } else {
                // Crosses midnight, e.g. 22:00 to 07:00
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }
}

