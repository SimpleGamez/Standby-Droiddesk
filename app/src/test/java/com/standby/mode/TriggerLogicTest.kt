package com.standby.mode

import com.google.common.truth.Truth.assertThat
import com.standby.mode.domain.model.TriggerConfig
import org.junit.Test

class TriggerLogicTest {

    private fun evaluateTrigger(
        isCharging: Boolean,
        isLandscape: Boolean,
        isLocked: Boolean,
        isStationary: Boolean,
        config: TriggerConfig
    ): Boolean {
        if (config.manualOnly) return false
        if (!isCharging) return false

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

    @Test
    fun testNotChargingNeverTriggers() {
        val config = TriggerConfig(onChargerConnected = true)
        val result = evaluateTrigger(
            isCharging = false,
            isLandscape = true,
            isLocked = true,
            isStationary = true,
            config = config
        )
        assertThat(result).isFalse()
    }

    @Test
    fun testManualOnlyNeverTriggersAutomatically() {
        val config = TriggerConfig(manualOnly = true, onChargerConnected = true)
        val result = evaluateTrigger(
            isCharging = true,
            isLandscape = true,
            isLocked = true,
            isStationary = true,
            config = config
        )
        assertThat(result).isFalse()
    }

    @Test
    fun testChargingAndLandscapeTrigger() {
        val config = TriggerConfig(
            onChargerConnected = false,
            onChargingAndLandscape = true
        )

        // Portrait: should NOT trigger
        val portraitResult = evaluateTrigger(
            isCharging = true,
            isLandscape = false,
            isLocked = false,
            isStationary = true,
            config = config
        )
        assertThat(portraitResult).isFalse()

        // Landscape: should trigger
        val landscapeResult = evaluateTrigger(
            isCharging = true,
            isLandscape = true,
            isLocked = false,
            isStationary = true,
            config = config
        )
        assertThat(landscapeResult).isTrue()
    }

    @Test
    fun testChargingAndStationaryTrigger() {
        val config = TriggerConfig(
            onChargerConnected = false,
            onChargingAndStationary = true
        )

        // Moving: should NOT trigger
        val movingResult = evaluateTrigger(
            isCharging = true,
            isLandscape = false,
            isLocked = false,
            isStationary = false,
            config = config
        )
        assertThat(movingResult).isFalse()

        // Stationary: should trigger
        val stationaryResult = evaluateTrigger(
            isCharging = true,
            isLandscape = false,
            isLocked = false,
            isStationary = true,
            config = config
        )
        assertThat(stationaryResult).isTrue()
    }
}

