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
    val items: List<HomeListItem> = emptyList(),
    val gridSections: List<GridDaySection> = emptyList(),
    val momentCells: List<GridCellUiState> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasReachedEarliest: Boolean = false,
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
    val idleStartTime: LocalTime? = null,
    val idleEndTime: LocalTime? = null,
    val editBehaviorId: Long? = null,
    val editInitialActivityId: Long? = null,
    val editInitialTagIds: List<Long> = emptyList(),
    val editInitialNote: String? = null,
)
