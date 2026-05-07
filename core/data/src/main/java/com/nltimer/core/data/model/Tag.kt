package com.nltimer.core.data.model

import androidx.compose.runtime.Immutable

/**
 * Tag 标签领域模型
 * 用于给行为/活动打标签分类，包含颜色、图标、优先级等展示属性
 */
@Immutable
data class Tag(
    val id: Long,
    val name: String,
    val color: Long?,
    val iconKey: String?,
    val category: String?,
    val priority: Int,
    val usageCount: Int,
    val sortOrder: Int,
    val isArchived: Boolean,
    val archivedAt: Long? = null,
    val keywords: String? = null,
)
