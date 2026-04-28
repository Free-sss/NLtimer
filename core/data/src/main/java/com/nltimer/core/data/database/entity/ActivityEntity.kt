package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val iconKey: String? = null,
    val groupId: Long? = null,
    val isPreset: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
