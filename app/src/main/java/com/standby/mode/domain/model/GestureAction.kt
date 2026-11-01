package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GestureAction {
    NEXT_PAGE, PREVIOUS_PAGE, SHOW_CONTROLS, TOGGLE_DIM,
    OPEN_SETTINGS, EXIT_STANDBY, NONE
}
