package com.standby.mode

import com.google.common.truth.Truth.assertThat
import com.standby.mode.domain.model.BrightnessConfig
import com.standby.mode.domain.model.BurnInConfig
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ColorPalette
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.PageLayout
import com.standby.mode.domain.model.StandbyModule
import com.standby.mode.domain.model.StandbyProfile
import com.standby.mode.domain.model.StandbySettings
import com.standby.mode.domain.model.ThemeConfig
import com.standby.mode.domain.model.TriggerConfig
import com.standby.mode.ui.settings.StandbyBackup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class LayoutSerializationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testModuleSerialization() {
        val module = StandbyModule(
            id = "mod_1",
            type = ModuleType.CLOCK,
            row = 0,
            column = 0,
            rowSpan = 2,
            colSpan = 2,
            config = mapOf("style" to ClockStyle.FLIP.name)
        )

        val serialized = json.encodeToString(module)
        val deserialized = json.decodeFromString<StandbyModule>(serialized)

        assertThat(deserialized.id).isEqualTo("mod_1")
        assertThat(deserialized.type).isEqualTo(ModuleType.CLOCK)
        assertThat(deserialized.config["style"]).isEqualTo("FLIP")
    }

    @Test
    fun testProfileSerializationAndBackup() {
        val page = PageLayout(
            id = "page_1",
            name = "Main",
            sortOrder = 0,
            modules = listOf(
                StandbyModule(
                    id = "mod_clock",
                    type = ModuleType.CLOCK,
                    row = 0,
                    column = 0,
                    rowSpan = 2,
                    colSpan = 2
                ),
                StandbyModule(
                    id = "mod_weather",
                    type = ModuleType.WEATHER,
                    row = 0,
                    column = 2,
                    rowSpan = 2,
                    colSpan = 2
                )
            )
        )

        val profile = StandbyProfile(
            id = "profile_desk",
            name = "Desk",
            pages = listOf(page),
            triggerConfig = TriggerConfig(
                onChargerConnected = true,
                onChargingAndLandscape = true
            ),
            themeConfig = ThemeConfig(
                colorPalette = ColorPalette.SUNSET,
                useGradientBackground = true
            ),
            brightnessConfig = BrightnessConfig(),
            burnInConfig = BurnInConfig(pixelShiftEnabled = true),
            isActive = true
        )

        val backup = StandbyBackup(
            version = 1,
            settings = StandbySettings(is24Hour = false, showSeconds = true),
            profiles = listOf(profile)
        )

        val serialized = json.encodeToString(backup)
        val deserialized = json.decodeFromString<StandbyBackup>(serialized)

        assertThat(deserialized.version).isEqualTo(1)
        assertThat(deserialized.settings.is24Hour).isFalse()
        assertThat(deserialized.settings.showSeconds).isTrue()
        assertThat(deserialized.profiles).hasSize(1)
        assertThat(deserialized.profiles.first().name).isEqualTo("Desk")
        assertThat(deserialized.profiles.first().pages.first().modules).hasSize(2)
        assertThat(deserialized.profiles.first().themeConfig.colorPalette).isEqualTo(ColorPalette.SUNSET)
    }
}

