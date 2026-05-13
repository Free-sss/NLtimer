package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
data class GridDaySection(
    val date: LocalDate,
    val label: String,
    val rows: List<GridRowUiState>,
)
