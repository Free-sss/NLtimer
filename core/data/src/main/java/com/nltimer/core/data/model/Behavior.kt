package com.nltimer.core.data.model

/**
 * Behavior 行为记录领域模型
 * 表示一次活动的完整计时记录，包含起止时间、状态、备注、番茄钟数量与完成度评估
 */
data class Behavior(
    val id: Long,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long?,
    val status: BehaviorNature,
    val note: String?,
    val pomodoroCount: Int,
    val sequence: Int,
    val estimatedDuration: Long?,
    val actualDuration: Long?,
    val achievementLevel: Int?,
    val wasPlanned: Boolean,
)
