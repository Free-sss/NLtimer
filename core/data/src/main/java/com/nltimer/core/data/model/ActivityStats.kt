package com.nltimer.core.data.model

import androidx.compose.runtime.Immutable

/**
 * ActivityStats 活动统计数据模型
 * 包含使用次数、总计时长（分钟）和最后使用时间戳
 */
@Immutable
data class ActivityStats(
    val usageCount: Int = 0,
    val totalDurationMinutes: Long = 0,
    val lastUsedTimestamp: Long? = null,
)
