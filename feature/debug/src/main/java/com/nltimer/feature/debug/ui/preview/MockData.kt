package com.nltimer.feature.debug.ui.preview

import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag

object MockData {
    val tags = listOf(
        Tag(
            id = 1, name = "工作", color = 0xFF4A90E2, textColor = null,
            icon = null, category = "生活", priority = 0,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 2, name = "学习", color = 0xFF50C878, textColor = null,
            icon = null, category = "成长", priority = 1,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 3, name = "运动", color = 0xFFFF6B6B, textColor = null,
            icon = null, category = "健康", priority = 2,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 4, name = "深度", color = 0xFF9B59B6, textColor = null,
            icon = null, category = null, priority = 0,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
        Tag(
            id = 5, name = "紧急", color = 0xFFE74C3C, textColor = null,
            icon = null, category = null, priority = 0,
            usageCount = 0, sortOrder = 0, isArchived = false,
        ),
    )

    val groups = listOf(
        ActivityGroup(id = 1, name = "工作", sortOrder = 0),
        ActivityGroup(id = 2, name = "学习", sortOrder = 1),
        ActivityGroup(id = 3, name = "健康", sortOrder = 2),
        ActivityGroup(id = 4, name = "娱乐", sortOrder = 3),
    )
}
