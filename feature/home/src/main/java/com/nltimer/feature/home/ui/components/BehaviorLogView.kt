package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.feature.home.model.GridCellUiState
@Composable
fun BehaviorLogView(
    cells: List<GridCellUiState>,
    onCellLongClick: (GridCellUiState) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val timeFormatter = hhmmFormatter

    val behaviors = remember(cells) {
        cells.filter { it.behaviorId != null && it.startTime != null }
            .sortedByDescending { it.startTime }
    }

    var detailCell by remember { mutableStateOf<GridCellUiState?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (behaviors.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无行为记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(items = behaviors, key = { it.behaviorId!! }) { behavior ->
                    BehaviorLogCard(
                        behavior = behavior,
                        timeFormatter = timeFormatter,
                        onClick = { detailCell = behavior },
                        onLongClick = { onCellLongClick(behavior) },
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    detailCell?.let { cell ->
        BehaviorDetailDialog(
            cell = cell,
            onDismiss = { detailCell = null },
        )
    }
}