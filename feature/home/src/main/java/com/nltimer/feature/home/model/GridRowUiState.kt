package com.nltimer.feature.home.model

import java.time.LocalTime

data class GridRowUiState(
    val rowId: String,
    val startTime: LocalTime,
    val isCurrentRow: Boolean,
    val isLocked: Boolean,
    val cells: List<GridCellUiState>,
)
