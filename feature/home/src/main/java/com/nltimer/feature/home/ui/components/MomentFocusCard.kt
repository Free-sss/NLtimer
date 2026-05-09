package com.nltimer.feature.home.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.ui.components.moment.ActiveCard
import com.nltimer.feature.home.ui.components.moment.EmptyCard
import com.nltimer.feature.home.ui.components.moment.PendingCard

@Composable
fun MomentFocusCard(
    activeCell: GridCellUiState?,
    nextPendingCell: GridCellUiState?,
    onCompleteBehavior: (Long) -> Unit,
    onStartNextPending: () -> Unit,
    onStartBehavior: (Long) -> Unit,
    onEmptyCellClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        activeCell != null -> ActiveCard(
            cell = activeCell,
            onComplete = { activeCell.behaviorId?.let(onCompleteBehavior) },
            modifier = modifier,
        )
        nextPendingCell != null -> PendingCard(
            cell = nextPendingCell,
            onStart = { nextPendingCell.behaviorId?.let(onStartBehavior) },
            modifier = modifier,
        )
        else -> EmptyCard(
            onClick = onEmptyCellClick,
            modifier = modifier,
        )
    }
}