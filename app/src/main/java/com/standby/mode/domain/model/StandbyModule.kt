package com.standby.mode.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class StandbyModule(
    val id: String = UUID.randomUUID().toString(),
    val type: ModuleType,
    val row: Int = 0,
    val column: Int = 0,
    val rowSpan: Int = 1,
    val colSpan: Int = 1,
    val config: Map<String, String> = emptyMap()
)
