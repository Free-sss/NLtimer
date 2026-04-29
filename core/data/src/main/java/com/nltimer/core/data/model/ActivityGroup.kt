package com.nltimer.core.data.model

import com.nltimer.core.data.database.entity.ActivityGroupEntity

/**
 * ActivityGroup 活动分组领域模型
 * 用于将活动按分类组织，支持自定义排序
 */
data class ActivityGroup(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
) {
    /** 转换为数据库实体 */
    fun toEntity() = ActivityGroupEntity(
        id = id,
        name = name,
        sortOrder = sortOrder,
    )

    companion object {
        /** 从数据库实体构建领域模型 */
        fun fromEntity(entity: ActivityGroupEntity) = ActivityGroup(
            id = entity.id,
            name = entity.name,
            sortOrder = entity.sortOrder,
        )
    }
}
