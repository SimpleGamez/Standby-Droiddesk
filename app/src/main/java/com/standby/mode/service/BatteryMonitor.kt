package com.standby.mode.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.standby.mode.domain.model.BatteryInfo
import com.standby.mode.domain.model.ChargeType
import com.standby.mode.domain.provider.BatteryProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatteryMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryProvider {

    private val _batteryInfo = MutableStateFlow(fetchCurrentBatteryInfo())
    override fun getBatteryInfo(): Flow<BatteryInfo> = _batteryInfo.asStateFlow()
    override fun getCurrentBatteryInfo(): BatteryInfo = _batteryInfo.value

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                _batteryInfo.value = parseBatteryIntent(it)
            }
        }
    }

    private var isListening = false

    fun startListening() {
        if (!isListening) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = context.registerReceiver(batteryReceiver, filter)
            stickyIntent?.let {
                _batteryInfo.value = parseBatteryIntent(it)
            }
            isListening = true
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            isListening = false
        }
    }

    private fun fetchCurrentBatteryInfo(): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        return if (intent != null) parseBatteryIntent(intent) else BatteryInfo()
    }

    private fun parseBatteryIntent(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val chargeType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> ChargeType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> ChargeType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargeType.WIRELESS
            else -> ChargeType.NONE
        }

        val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val temperature = tempTenths / 10.0f
        val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val voltage = voltageMv / 1000.0f

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentNow = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0

        val estimatedTimeToFull: Long? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            batteryManager?.computeChargeTimeRemaining()?.takeIf { it > 0 }
        } else {
            null
        }

        return BatteryInfo(
            level = batteryPct,
            isCharging = isCharging,
            chargeType = chargeType,
            temperature = temperature,
            voltage = voltage,
            currentNow = currentNow,
            estimatedTimeToFull = estimatedTimeToFull
        )
    }
}

