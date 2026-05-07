package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalDateTime

/**
 * 行为详情底部弹出页的 UI 状态。
 * 包含行为的完整信息、可选标签和可用活动列表。
 */
@Immutable
data class BehaviorDetailUiState(
    val behaviorId: Long,
    val activityId: Long,
    val activityIconKey: String?,
    val activityName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val status: BehaviorNature,
    val note: String?,
    val tags: List<TagUiState>,
    val allAvailableTags: List<TagUiState>,
    val allActivities: List<Activity>,
    val achievementLevel: Int?,
    val estimatedDuration: Long?,
    val actualDuration: Long?,
)
