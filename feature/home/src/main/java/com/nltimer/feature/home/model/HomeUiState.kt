package com.nltimer.feature.home.model

data class HomeUiState(
    val rows: List<GridRowUiState> = emptyList(),
    val currentRowId: String? = null,
    val isAddSheetVisible: Boolean = false,
    val selectedTimeHour: Int = 0,
    val isLoading: Boolean = true,
)
