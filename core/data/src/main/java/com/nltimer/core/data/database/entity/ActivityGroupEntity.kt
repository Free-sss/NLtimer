package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ActivityGroupEntity 活动分组数据库实体
 * 对应 activity_groups 表，存储分组名称、排序序号和创建时间
 */
@Entity(tableName = "activity_groups")
data class ActivityGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
