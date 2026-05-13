package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate

@Immutable
sealed interface HomeListItem {
    val key: String

    @Immutable
    data class CellItem(val cell: GridCellUiState) : HomeListItem {
        override val key: String = "cell-${cell.behaviorId ?: "null"}"
    }

    @Immutable
    data class DayDivider(val date: LocalDate, val label: String) : HomeListItem {
        override val key: String = "divider-${date}"
    }
}
