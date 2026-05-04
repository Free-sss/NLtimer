package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalTime

enum class AddSheetMode(val nature: BehaviorNature) {
    COMPLETED(BehaviorNature.COMPLETED),
    CURRENT(BehaviorNature.ACTIVE),
    TARGET(BehaviorNature.PENDING),
}

@Immutable
data class HomeUiState(
    val rows: List<GridRowUiState> = emptyList(),
    val currentRowId: String? = null,
    val addSheetMode: AddSheetMode? = null,
    val selectedTimeHour: Int = 0,
    val isLoading: Boolean = true,
    val isIdleMode: Boolean = false,
    val hasActiveBehavior: Boolean = false,
    val isDetailSheetVisible: Boolean = false,
    val detailBehavior: BehaviorDetailUiState? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val lastBehaviorEndTime: LocalTime? = null,
)
