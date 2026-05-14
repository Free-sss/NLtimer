package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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

private data class ItemLayoutInfo(
    val y: Float,
    val height: Float,
)

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
    val reorderedGroups = remember { mutableStateListOf<CategoryGroup<T>>() }

    LaunchedEffect(categoryGroups) {
        if (reorderedGroups.toList() != categoryGroups) {
            reorderedGroups.clear()
            reorderedGroups.addAll(categoryGroups)
        }
    }

    var draggedIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var currentTargetIndex by remember { mutableIntStateOf(-1) }

    val itemLayouts = remember { mutableStateMapOf<Int, ItemLayoutInfo>() }
    val shiftOffsets = remember { mutableStateMapOf<Int, Float>() }

    var allCollapsed by remember { mutableStateOf(false) }
    val collapsedStates = remember { mutableStateMapOf<Int, Boolean>() }

    fun computeTargetIndex(draggedIdx: Int, offsetY: Float): Int {
        val draggedInfo = itemLayouts[draggedIdx] ?: return draggedIdx
        val draggedCenter = draggedInfo.y + draggedInfo.height / 2f + offsetY
        val spacing = 8.dp.value

        var bestIndex = draggedIdx
        var bestDist = Float.MAX_VALUE

        for ((idx, info) in itemLayouts) {
            if (idx == draggedIdx) continue
            val itemCenter = info.y + info.height / 2f
            val dist = kotlin.math.abs(draggedCenter - itemCenter)
            if (dist < bestDist && dist < info.height / 2f + draggedInfo.height / 2f + spacing) {
                bestDist = dist
                bestIndex = idx
            }
        }

        if (bestIndex == draggedIdx) return draggedIdx

        val targetInfo = itemLayouts[bestIndex] ?: return draggedIdx
        val targetCenter = targetInfo.y + targetInfo.height / 2f

        return if (draggedCenter < targetCenter) {
            if (bestIndex > draggedIdx) bestIndex - 1 else bestIndex
        } else {
            if (bestIndex < draggedIdx) bestIndex + 1 else bestIndex
        }
    }

    fun computeShiftOffsets(draggedIdx: Int, targetIdx: Int) {
        shiftOffsets.clear()
        if (draggedIdx == -1 || targetIdx == -1 || draggedIdx == targetIdx) return

        val draggedInfo = itemLayouts[draggedIdx] ?: return
        val draggedHeight = draggedInfo.height
        val spacing = 8.dp.value

        if (targetIdx > draggedIdx) {
            for (i in (draggedIdx + 1)..targetIdx) {
                val info = itemLayouts[i] ?: continue
                shiftOffsets[i] = -(draggedHeight + spacing)
            }
        } else {
            for (i in targetIdx until draggedIdx) {
                val info = itemLayouts[i] ?: continue
                shiftOffsets[i] = draggedHeight + spacing
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { CategoryPickerTitle(title, sortMode) { sortMode = it } },
        text = {
            val listState = rememberLazyListState()
            val density = androidx.compose.ui.platform.LocalDensity.current
            val scrollZonePx = with(density) { 48.dp.toPx() }

            LaunchedEffect(draggedIndex, dragOffsetY) {
                if (draggedIndex == -1) return@LaunchedEffect
                val draggedInfo = itemLayouts[draggedIndex] ?: return@LaunchedEffect
                val itemTop = draggedInfo.y + dragOffsetY
                val itemBottom = itemTop + draggedInfo.height
                val viewportTop = 0f
                val viewportSize = listState.layoutInfo.viewportSize.height.toFloat()
                val viewportBottom = viewportTop + viewportSize

                val scrollAmount = when {
                    itemTop < viewportTop + scrollZonePx -> {
                        val penetration = (viewportTop + scrollZonePx - itemTop).coerceAtMost(scrollZonePx)
                        -penetration / scrollZonePx * 8f
                    }
                    itemBottom > viewportBottom - scrollZonePx -> {
                        val penetration = (itemBottom - (viewportBottom - scrollZonePx)).coerceAtMost(scrollZonePx)
                        penetration / scrollZonePx * 8f
                    }
                    else -> 0f
                }

                if (scrollAmount != 0f) {
                    val current = listState.firstVisibleItemIndex
                    val offset = listState.firstVisibleItemScrollOffset
                    listState.scrollToItem(current, (offset + scrollAmount).toInt())
                }
            }

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

                        val collapsed = allCollapsed || (collapsedStates[index] == true)

                        CategoryGroupCard(
                            index = index,
                            groupName = group.name,
                            items = sortedItems,
                            selectedId = selectedId,
                            selectedIds = selectedIds,
                            multiSelect = multiSelect,
                            onItemSelected = onItemSelected,
                            onItemsSelected = onItemsSelected,
                            isDragging = draggedIndex == index,
                            dragOffsetY = if (draggedIndex == index) dragOffsetY else 0f,
                            shiftOffset = shiftOffsets[index] ?: 0f,
                            collapsed = collapsed,
                            onDragStart = {
                                draggedIndex = index
                                currentTargetIndex = index
                                dragOffsetY = 0f
                            },
                            onDrag = { delta ->
                                dragOffsetY += delta
                                val target = computeTargetIndex(index, dragOffsetY)
                                if (target != currentTargetIndex) {
                                    currentTargetIndex = target
                                    computeShiftOffsets(index, target)
                                }
                            },
                            onDragEnd = {
                                val target = currentTargetIndex
                                if (draggedIndex != -1 && target != -1 && target != draggedIndex && target in reorderedGroups.indices) {
                                    val item = reorderedGroups.removeAt(draggedIndex)
                                    reorderedGroups.add(target.coerceIn(0, reorderedGroups.size), item)
                                    onCategoryReordered(reorderedGroups.map { it.id })
                                }
                                draggedIndex = -1
                                dragOffsetY = 0f
                                currentTargetIndex = -1
                                shiftOffsets.clear()
                            },
                            onDragCancel = {
                                draggedIndex = -1
                                dragOffsetY = 0f
                                currentTargetIndex = -1
                                shiftOffsets.clear()
                            },
                            onPositioned = { idx, y, height ->
                                itemLayouts[idx] = ItemLayoutInfo(y, height)
                            },
                        )
                    }
                }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = {
                        allCollapsed = !allCollapsed
                        if (allCollapsed) {
                            reorderedGroups.indices.forEach { collapsedStates[it] = true }
                        } else {
                            collapsedStates.clear()
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(
                        imageVector = if (allCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = if (allCollapsed) "展开全部" else "收纳全部",
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (onAddNew != null) {
                    TextButton(onClick = onAddNew) {
                        Text("添加")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
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
                icon = Icons.AutoMirrored.Filled.TrendingUp,
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
