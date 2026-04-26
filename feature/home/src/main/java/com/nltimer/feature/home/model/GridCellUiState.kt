package com.nltimer.feature.home.model

import com.nltimer.core.data.model.BehaviorNature

data class GridCellUiState(
    val behaviorId: Long?,
    val activityEmoji: String?,
    val activityName: String?,
    val tags: List<TagUiState>,
    val nature: BehaviorNature?,
    val isCurrent: Boolean,
)
