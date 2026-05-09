package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun <T : CategorizableItem> CategoryPickerDialog(
    title: String,
    items: List<T>,
    categoryGroups: List<CategoryGroup<T>>,
    selectedId: Long? = null,
    selectedIds: Set<Long> = emptySet(),
    multiSelect: Boolean = false,
    onItemSelected: (Long) -> Unit = {},
    onItemsSelected: (Set<Long>) -> Unit = {},
    onCategoryReordered: (List<Long>) -> Unit = {},
    onDismiss: () -> Unit,
    onAddNew: (() -> Unit)? = null,
) {
    var sortMode by remember { mutableStateOf(SortMode.FREQUENCY) }
    val reorderedGroups = remember { mutableStateListOf<CategoryGroup<T>>().apply { addAll(categoryGroups) } }
    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { CategoryPickerTitle(title, sortMode) { sortMode = it } },
        text = {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                itemsIndexed(
                    items = reorderedGroups,
                    key = { _, group -> group.id }
                ) { index, group ->
                    val sortedItems = when (sortMode) {
                        SortMode.FREQUENCY -> group.items.sortedByDescending { it.usageCount }
                        SortMode.ALPHA -> group.items.sortedBy { it.itemName }
                        SortMode.RECENT -> group.items.sortedByDescending { it.lastUsedTimestamp ?: 0L }
                    }

                    CategoryGroupCard(
                        groupName = group.name,
                        items = sortedItems,
                        selectedId = selectedId,
                        selectedIds = selectedIds,
                        multiSelect = multiSelect,
                        onItemSelected = onItemSelected,
                        onItemsSelected = onItemsSelected,
                        isDragging = draggedIndex == index,
                        dragOffsetY = if (draggedIndex == index) dragOffsetY else 0f,
                        onDragStart = { draggedIndex = index },
                        onDrag = { dragOffsetY += it },
                        onDragEnd = {
                            if (draggedIndex != -1 && dragOffsetY != 0f) {
                                val targetIndex = calculateTargetIndex(
                                    draggedIndex,
                                    dragOffsetY,
                                    reorderedGroups.size,
                                )
                                if (targetIndex != draggedIndex && targetIndex in reorderedGroups.indices) {
                                    val item = reorderedGroups.removeAt(draggedIndex)
                                    reorderedGroups.add(targetIndex, item)
                                    onCategoryReordered(reorderedGroups.map { it.id })
                                }
                            }
                            draggedIndex = -1
                            dragOffsetY = 0f
                        },
                        onDragCancel = {
                            draggedIndex = -1
                            dragOffsetY = 0f
                        },
                    )
                }
            }
        },
        confirmButton = {
            if (onAddNew != null) {
                TextButton(onClick = onAddNew) {
                    Text("添加")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

@Composable
private fun CategoryPickerTitle(
    title: String,
    currentSortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SortChip(
                label = "频率",
                icon = Icons.Default.TrendingUp,
                selected = currentSortMode == SortMode.FREQUENCY,
                onClick = { onSortModeChange(SortMode.FREQUENCY) },
            )
            SortChip(
                label = "字母",
                icon = Icons.Default.SortByAlpha,
                selected = currentSortMode == SortMode.ALPHA,
                onClick = { onSortModeChange(SortMode.ALPHA) },
            )
            SortChip(
                label = "最近",
                icon = Icons.Default.Schedule,
                selected = currentSortMode == SortMode.RECENT,
                onClick = { onSortModeChange(SortMode.RECENT) },
            )
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                maxLines = 1,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
            )
        },
        modifier = Modifier.height(24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent,
            enabled = true,
            selected = selected,
        ),
    )
}

