package com.standby.mode.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PageLayout(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Page",
    val modules: List<StandbyModule> = emptyList(),
    val sortOrder: Int = 0
)
