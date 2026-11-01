package com.standby.mode.domain.provider

import com.standby.mode.domain.model.BatteryInfo
import kotlinx.coroutines.flow.Flow

interface BatteryProvider {
    fun getBatteryInfo(): Flow<BatteryInfo>
    fun getCurrentBatteryInfo(): BatteryInfo
}
