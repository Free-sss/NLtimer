package com.nltimer.core.data.model

import androidx.compose.runtime.Immutable
import com.nltimer.core.data.database.entity.TagEntity

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
    val keywords: String?,
    val isArchived: Boolean,
    val archivedAt: Long? = null,
) {
    fun toEntity() = TagEntity(
        id = id,
        name = name,
        color = color,
        iconKey = iconKey,
        category = category,
        priority = priority,
        usageCount = usageCount,
        sortOrder = sortOrder,
        keywords = keywords,
        isArchived = isArchived,
        archivedAt = archivedAt,
    )

    companion object {
        fun fromEntity(entity: TagEntity) = Tag(
            id = entity.id,
            name = entity.name,
            color = entity.color,
            iconKey = entity.iconKey,
            category = entity.category,
            priority = entity.priority,
            usageCount = entity.usageCount,
            sortOrder = entity.sortOrder,
            keywords = entity.keywords,
            isArchived = entity.isArchived,
            archivedAt = entity.archivedAt,
        )
    }
}
