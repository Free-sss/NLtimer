package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * TagEntity 标签数据库实体
 * 对应 tags 表，存储标签名称、颜色、图标、分类、优先级和使用统计
 */
@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Long? = null,
    val iconKey: String? = null,
    val category: String? = null,
    val priority: Int = 0,
    val usageCount: Int = 0,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val archivedAt: Long? = null,
)
