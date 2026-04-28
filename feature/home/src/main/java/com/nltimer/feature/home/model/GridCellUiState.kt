package com.nltimer.feature.home.model

import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalTime

data class GridCellUiState(
    val behaviorId: Long?,
    val activityEmoji: String?,
    val activityName: String?,
    val tags: List<TagUiState>,
    val status: BehaviorNature?,
    val isCurrent: Boolean,
    val wasPlanned: Boolean = false,
    val achievementLevel: Int? = null,
    val estimatedDuration: Long? = null,
    val actualDuration: Long? = null,
    val durationMs: Long? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAddPlaceholder: Boolean = false,
    val note: String? = null,
)
