package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

private val MomentFilterTab.displayName: String
    get() = when (this) {
        MomentFilterTab.ALL -> "乃大"
        MomentFilterTab.COMPLETED -> "曾经"
        MomentFilterTab.PENDING -> "此后"
    }

@Composable
internal fun MomentFilterSortBar(
    filterTab: MomentFilterTab,
    onFilterChange: (MomentFilterTab) -> Unit,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    sortMode: MomentSortMode,
    onSortChange: (MomentSortMode) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
            TextButton(onClick = { onMenuExpandedChange(true) }) {
                Text(
                    "${filterTab.displayName} · ${sortMode.label}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { onMenuExpandedChange(false) },
            ) {
                MomentFilterTab.entries.forEach { tab ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                tab.displayName,
                                color = if (filterTab == tab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onClick = {
                            onFilterChange(tab)
                            onMenuExpandedChange(false)
                        },
                    )
                }
                HorizontalDivider()
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
                            onMenuExpandedChange(false)
                        },
                    )
                }
            }
        }
    }
}
