package com.standby.mode.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChargeType {
    AC, USB, WIRELESS, NONE
}
