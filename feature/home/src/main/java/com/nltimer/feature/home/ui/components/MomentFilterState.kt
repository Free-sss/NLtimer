package com.nltimer.feature.home.ui.components

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf

data class MomentFilterState(
    val filterKey: String = "ALL",
    val sortKey: String = "TIME_DESC",
    val onFilterChange: (String) -> Unit = {},
    val onSortChange: (String) -> Unit = {},
)

val LocalMomentFilterState = compositionLocalOf { MomentFilterState() }

val LocalVisibleDateLabel = staticCompositionLocalOf { mutableStateOf<String?>(null) }
