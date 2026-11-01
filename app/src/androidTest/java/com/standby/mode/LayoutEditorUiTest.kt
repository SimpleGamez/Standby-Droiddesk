package com.standby.mode

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.standby.mode.domain.model.ClockStyle
import com.standby.mode.domain.model.ModuleType
import com.standby.mode.domain.model.StandbyModule
import com.standby.mode.ui.editor.LayoutEditorScreen
import com.standby.mode.ui.modules.ClockModule
import com.standby.mode.ui.theme.StandByTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LayoutEditorUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testClockModuleDisplaysCorrectly() {
        composeTestRule.setContent {
            StandByTheme {
                ClockModule(
                    style = ClockStyle.DIGITAL,
                    is24Hour = true,
                    showSeconds = false,
                    dateFormat = "EEEE, MMMM d"
                )
            }
        }

        // Verify clock module renders without crashing
        composeTestRule.waitForIdle()
    }
}

