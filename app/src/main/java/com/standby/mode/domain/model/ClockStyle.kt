package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ClockStyle {
    DIGITAL, ANALOG, FLIP, MINIMAL, BOLD, WORD_CLOCK, BINARY, VERTICAL_STACK
}
