package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun MomentFilterSortBar(
    filterTab: MomentFilterTab,
    onFilterChange: (MomentFilterTab) -> Unit,
    sortMode: MomentSortMode,
    onSortChange: (MomentSortMode) -> Unit,
    sortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        FilterChip(
            selected = filterTab == MomentFilterTab.ALL,
            onClick = { onFilterChange(MomentFilterTab.ALL) },
            label = { Text("全部", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.padding(end = 4.dp),
        )
        FilterChip(
            selected = filterTab == MomentFilterTab.COMPLETED,
            onClick = { onFilterChange(MomentFilterTab.COMPLETED) },
            label = { Text("经过", style = MaterialTheme.typography.labelSmall) },
            modifier = Modifier.padding(end = 4.dp),
        )
        FilterChip(
            selected = filterTab == MomentFilterTab.PENDING,
            onClick = { onFilterChange(MomentFilterTab.PENDING) },
            label = { Text("目标", style = MaterialTheme.typography.labelSmall) },
        )

        Spacer(Modifier.weight(1f))

        Box {
            IconButton(onClick = { onSortMenuExpandedChange(true) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "排序",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { onSortMenuExpandedChange(false) },
            ) {
                MomentSortMode.entries.forEach { mode ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                mode.label,
                                color = if (sortMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onSortChange(mode)
                            onSortMenuExpandedChange(false)
                        },
                    )
                }
            }
        }
    }
}
