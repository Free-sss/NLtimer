package com.nltimer.core.data.model

import androidx.compose.runtime.Immutable
import com.nltimer.core.data.database.entity.BehaviorEntity

/**
 * Behavior 行为记录领域模型
 * 表示一次活动的完整计时记录，包含起止时间、状态、备注、番茄钟数量与完成度评估
 */
@Immutable
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
) {
    fun toEntity() = BehaviorEntity(
        id = id,
        activityId = activityId,
        startTime = startTime,
        endTime = endTime,
        status = status.key,
        note = note,
        pomodoroCount = pomodoroCount,
        sequence = sequence,
        estimatedDuration = estimatedDuration,
        actualDuration = actualDuration,
        achievementLevel = achievementLevel,
        wasPlanned = wasPlanned,
    )

    companion object {
        fun fromEntity(entity: BehaviorEntity) = Behavior(
            id = entity.id,
            activityId = entity.activityId,
            startTime = entity.startTime,
            endTime = entity.endTime,
            status = BehaviorNature.entries.firstOrNull { it.key == entity.status } ?: BehaviorNature.PENDING,
            note = entity.note,
            pomodoroCount = entity.pomodoroCount,
            sequence = entity.sequence,
            estimatedDuration = entity.estimatedDuration,
            actualDuration = entity.actualDuration,
            achievementLevel = entity.achievementLevel,
            wasPlanned = entity.wasPlanned,
        )
    }
}
