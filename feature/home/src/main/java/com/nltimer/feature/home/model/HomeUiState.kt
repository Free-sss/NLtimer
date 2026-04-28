package com.nltimer.feature.home.model

data class HomeUiState(
    val rows: List<GridRowUiState> = emptyList(),
    val currentRowId: String? = null,
    val isAddSheetVisible: Boolean = false,
    val selectedTimeHour: Int = 0,
    val isLoading: Boolean = true,
    val isIdleMode: Boolean = false,
    val hasActiveBehavior: Boolean = false,
    val isDetailSheetVisible: Boolean = false,
    val detailBehavior: BehaviorDetailUiState? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
