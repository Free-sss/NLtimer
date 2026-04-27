package com.nltimer.feature.home.model

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalDateTime

data class BehaviorDetailUiState(
    val behaviorId: Long,
    val activityId: Long,
    val activityEmoji: String?,
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
