package com.nltimer.feature.home.ui.sheet

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 排序方式枚举
 */
enum class SortMode(val label: String) {
    FREQUENCY("频率"),
    ALPHA("字母"),
    RECENT("最近"),
}

/**
 * 分类组数据
 */
data class CategoryGroup<T>(
    val id: Long,
    val name: String,
    val items: List<T>,
)

/**
 * 带分类信息的可选项
 */
interface CategorizableItem {
    val itemId: Long
    val itemName: String
    val category: String?
    val usageCount: Int
    val lastUsedTimestamp: Long?
    val emoji: String?
}

/**
 * 分类选择弹窗
 *
 * @param title 弹窗标题
 * @param items 所有可选项（需实现 CategorizableItem）
 * @param categoryGroups 分类组列表（用于排序和分组）
 * @param selectedId 当前选中的项 ID
 * @param selectedIds 多选模式下的选中 ID 集合
 * @param multiSelect 是否多选
 * @param onItemSelected 单选回调
 * @param onItemsSelected 多选回调
 * @param onCategoryReordered 分类重排序回调
 * @param onDismiss 关闭弹窗回调
 * @param onAddNew 点击添加按钮回调（可选）
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
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
        title = {
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
                        selected = sortMode == SortMode.FREQUENCY,
                        onClick = { sortMode = SortMode.FREQUENCY },
                    )
                    SortChip(
                        label = "字母",
                        icon = Icons.Default.SortByAlpha,
                        selected = sortMode == SortMode.ALPHA,
                        onClick = { sortMode = SortMode.ALPHA },
                    )
                    SortChip(
                        label = "最近",
                        icon = Icons.Default.Schedule,
                        selected = sortMode == SortMode.RECENT,
                        onClick = { sortMode = SortMode.RECENT },
                    )
                }
            }
        },
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
private fun SortChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun <T : CategorizableItem> CategoryGroupCard(
    groupName: String,
    items: List<T>,
    selectedId: Long?,
    selectedIds: Set<Long>,
    multiSelect: Boolean,
    onItemSelected: (Long) -> Unit,
    onItemsSelected: (Set<Long>) -> Unit,
    isDragging: Boolean,
    dragOffsetY: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, if (isDragging) dragOffsetY.roundToInt() else 0) }
            .shadow(
                elevation = if (isDragging) 8.dp else 0.dp,
                shape = RoundedCornerShape(8.dp),
            )
            .animateContentSize(),
        shape = RoundedCornerShape(8.dp),
        color = if (isDragging) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "拖拽排序",
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(0.5f)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onDrag(dragAmount.y)
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragCancel() },
                            )
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${items.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items.forEach { item ->
                    val isSelected = if (multiSelect) {
                        item.itemId in selectedIds
                    } else {
                        item.itemId == selectedId
                    }
                    ItemChip(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            if (multiSelect) {
                                val newIds = if (isSelected) selectedIds - item.itemId else selectedIds + item.itemId
                                onItemsSelected(newIds)
                            } else {
                                onItemSelected(item.itemId)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T : CategorizableItem> ItemChip(
    item: T,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.emoji != null) {
                Text(
                    text = item.emoji!!,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = item.itemName,
                fontSize = 12.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun calculateTargetIndex(
    draggedIndex: Int,
    dragOffsetY: Float,
    totalItems: Int,
): Int {
    val itemHeight = 80f // 近似高度
    val offset = (dragOffsetY / itemHeight).roundToInt()
    return (draggedIndex + offset).coerceIn(0, totalItems - 1)
}
