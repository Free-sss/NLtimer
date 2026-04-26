package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long? = null,
    val textColor: Long? = null,
    val icon: String? = null,
    val category: String? = null,
    val priority: Int = 0,
    val usageCount: Int = 0,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
)
