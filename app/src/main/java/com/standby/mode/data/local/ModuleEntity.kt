package com.standby.mode.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "modules",
    foreignKeys = [
        ForeignKey(
            entity = PageEntity::class,
            parentColumns = ["id"],
            childColumns = ["pageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pageId")]
)
data class ModuleEntity(
    @PrimaryKey
    val id: String,
    val pageId: String,
    val type: String,
    val row: Int,
    val column: Int,
    val rowSpan: Int,
    val colSpan: Int,
    val config: String // JSON map stored as String
)
