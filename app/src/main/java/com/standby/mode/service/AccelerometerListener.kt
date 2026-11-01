package com.standby.mode.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

@Singleton
class AccelerometerListener @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isStationary = MutableStateFlow(true)
    val isStationary: StateFlow<Boolean> = _isStationary.asStateFlow()

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private var isFirstSample = true
    private var stationaryCount = 0
    private var movementCount = 0

    private val movementThreshold = 0.35f
    private val stationaryThreshold = 15 // consecutive samples with delta below threshold

    private var isListening = false

    fun startListening() {
        if (!isListening && accelerometer != null) {
            isFirstSample = true
            stationaryCount = 0
            movementCount = 0
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL
            )
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
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (isFirstSample) {
            lastX = x
            lastY = y
            lastZ = z
            isFirstSample = false
            return
        }

        val deltaX = abs(x - lastX)
        val deltaY = abs(y - lastY)
        val deltaZ = abs(z - lastZ)
        val deltaMagnitude = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        lastX = x
        lastY = y
        lastZ = z

        if (deltaMagnitude > movementThreshold) {
            movementCount++
            stationaryCount = 0
            if (movementCount >= 3) {
                _isStationary.value = false
            }
        } else {
            stationaryCount++
            movementCount = 0
            if (stationaryCount >= stationaryThreshold) {
                _isStationary.value = true
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}

