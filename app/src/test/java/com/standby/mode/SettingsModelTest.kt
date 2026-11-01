package com.standby.mode

import com.google.common.truth.Truth.assertThat
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.GestureAction
import com.standby.mode.domain.model.StandbySettings
import com.standby.mode.domain.model.TemperatureUnit
import org.junit.Test

class SettingsModelTest {

    @Test
    fun testDefaultSettings() {
        val settings = StandbySettings()

        assertThat(settings.is24Hour).isTrue()
        assertThat(settings.showSeconds).isFalse()
        assertThat(settings.clockStyle).isEqualTo(ClockStyle.DIGITAL)
        assertThat(settings.exitOnUnplug).isTrue()
        assertThat(settings.keepScreenOn).isTrue()
        assertThat(settings.showOnLockScreen).isTrue()
        assertThat(settings.tapAction).isEqualTo(GestureAction.SHOW_CONTROLS)
        assertThat(settings.temperatureUnit).isEqualTo(TemperatureUnit.CELSIUS)
    }

    @Test
    fun testSettingsCopyTransformations() {
        val original = StandbySettings()
        val modified = original.copy(
            is24Hour = false,
            showSeconds = true,
            clockStyle = ClockStyle.ANALOG,
            temperatureUnit = TemperatureUnit.FAHRENHEIT
        )

        assertThat(modified.is24Hour).isFalse()
        assertThat(modified.showSeconds).isTrue()
        assertThat(modified.clockStyle).isEqualTo(ClockStyle.ANALOG)
        assertThat(modified.temperatureUnit).isEqualTo(TemperatureUnit.FAHRENHEIT)
    }
}

