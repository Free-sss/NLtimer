package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ActivityEntity 活动数据库实体
 * 对应 activities 表，存储活动名称、图标、分组、预设/归档状态和时间戳
 */
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
    val color: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
