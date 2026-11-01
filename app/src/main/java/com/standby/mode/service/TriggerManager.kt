package com.standby.mode.service

import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.view.OrientationEventListener
import com.standby.mode.data.repository.ProfileRepository
import com.standby.mode.data.repository.SettingsRepository
import com.standby.mode.domain.model.TriggerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TriggerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryMonitor: BatteryMonitor,
    private val accelerometerListener: AccelerometerListener,
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager

    private val _isLandscape = MutableStateFlow(
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    )
    val isLandscape: StateFlow<Boolean> = _isLandscape.asStateFlow()

    private val _isScreenLocked = MutableStateFlow(keyguardManager?.isKeyguardLocked ?: false)
    val isScreenLocked: StateFlow<Boolean> = _isScreenLocked.asStateFlow()

    private val _shouldActivate = MutableStateFlow(false)
    val shouldActivate: StateFlow<Boolean> = _shouldActivate.asStateFlow()

    private val orientationListener = object : OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            if (orientation == ORIENTATION_UNKNOWN) return
            // Landscape is roughly 60..120 or 240..300 degrees
            val isNowLandscape = (orientation in 60..120) || (orientation in 240..300)
            if (_isLandscape.value != isNowLandscape) {
                _isLandscape.value = isNowLandscape
                checkTriggerConditions()
            }
        }
    }

    private var isMonitoring = false

    fun startMonitoring() {
        if (isMonitoring) return
        batteryMonitor.startListening()
        accelerometerListener.startListening()
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }
        isMonitoring = true

        scope.launch {
            combine(
                batteryMonitor.getBatteryInfo(),
                accelerometerListener.isStationary,
                _isLandscape,
                profileRepository.getActiveProfile()
            ) { battery, stationary, landscape, activeProfile ->
                val config = activeProfile?.triggerConfig ?: TriggerConfig()
                evaluateConditions(
                    isCharging = battery.isCharging,
                    isLandscape = landscape,
                    isLocked = keyguardManager?.isKeyguardLocked ?: false,
                    isStationary = stationary,
                    config = config
                )
            }.collect { shouldRun ->
                _shouldActivate.value = shouldRun
            }
        }
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        orientationListener.disable()
        accelerometerListener.stopListening()
        batteryMonitor.stopListening()
        isMonitoring = false
    }

    fun updateScreenLockState(locked: Boolean) {
        _isScreenLocked.value = locked
        checkTriggerConditions()
    }

    private fun checkTriggerConditions() {
        val battery = batteryMonitor.getCurrentBatteryInfo()
        val stationary = accelerometerListener.isStationary.value
        val landscape = _isLandscape.value
        val locked = keyguardManager?.isKeyguardLocked ?: false

        scope.launch {
            val activeProfile = profileRepository.getActiveProfile()
            // We can read the current value or evaluate
        }
    }

    fun evaluateConditions(
        isCharging: Boolean,
        isLandscape: Boolean,
        isLocked: Boolean,
        isStationary: Boolean,
        config: TriggerConfig
    ): Boolean {
        if (config.manualOnly) return false
        if (!isCharging) return false

        // If any of the charging conditions match:
        var matched = false

        if (config.onChargerConnected) {
            matched = true
        }

        if (config.onChargingAndLandscape) {
            if (isLandscape) matched = true else return false
        }

        if (config.onChargingAndLocked) {
            if (isLocked) matched = true else return false
        }

        if (config.onChargingAndStationary) {
            if (isStationary) matched = true else return false
        }

        return matched
    }
}

