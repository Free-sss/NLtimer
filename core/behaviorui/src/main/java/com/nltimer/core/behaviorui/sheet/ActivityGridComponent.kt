package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.GridLayoutMode

private const val UNLIMITED_COUNT = Int.MAX_VALUE

@Immutable
data class ChipItem(
    val id: Long,
    val name: String,
    val color: Color,
)

fun ChipItem(activity: Activity): ChipItem {
    val color = activity.color?.let { Color(it) } ?: Color.Gray
    return ChipItem(id = activity.id, name = activity.name, color = color)
}

fun ChipItem(tag: Tag): ChipItem {
    val color = tag.color?.let { Color(it.toLong()) } ?: Color.Gray
    return ChipItem(id = tag.id, name = tag.name, color = color)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ActivityGridComponent(
    modifier: Modifier = Modifier,
    chips: List<ChipItem>,
    onChipClick: (Long) -> Unit,
    selectedId: Long? = null,
    selectedIds: Set<Long> = emptySet(),
    functionChipLabel: String = "管理",
    functionChipIcon: @Composable (() -> Unit)? = null,
    functionChipOnClick: () -> Unit = {},
    functionChipOnLongClick: () -> Unit = {},
    displayMode: ChipDisplayMode = ChipDisplayMode.Filled,
    layoutMode: GridLayoutMode = GridLayoutMode.Horizontal,
    maxLinesPerColumn: Int = 2,
    maxLinesHorizontal: Int = 2,
    useAdaptiveWidth: Boolean = true,
    chipMaxWidth: Dp = 120.dp,
    chipFixedWidth: Dp = 80.dp,
    useActivityColorForText: Boolean = true,
    multiSelect: Boolean = false,
) {
    val onContentColor = MaterialTheme.colorScheme.secondary
    val labelWidth = 64.dp
    val functionChipSpacing = 3.dp

    if (layoutMode == GridLayoutMode.Vertical) {
        VerticalGridLayout(
            modifier = modifier,
            chips = chips,
            onChipClick = onChipClick,
            selectedId = selectedId,
            selectedIds = selectedIds,
            functionChipLabel = functionChipLabel,
            functionChipIcon = functionChipIcon,
            functionChipOnClick = functionChipOnClick,
            functionChipOnLongClick = functionChipOnLongClick,
            displayMode = displayMode,
            maxLinesPerColumn = maxLinesPerColumn,
            chipMaxWidth = chipMaxWidth,
            chipFixedWidth = chipFixedWidth,
            useAdaptiveWidth = useAdaptiveWidth,
            useActivityColorForText = useActivityColorForText,
            multiSelect = multiSelect,
            onContentColor = onContentColor,
            labelWidth = labelWidth,
            functionChipSpacing = functionChipSpacing,
        )
    } else {
        HorizontalGridLayout(
            modifier = modifier,
            chips = chips,
            onChipClick = onChipClick,
            selectedId = selectedId,
            selectedIds = selectedIds,
            functionChipLabel = functionChipLabel,
            functionChipIcon = functionChipIcon,
            functionChipOnClick = functionChipOnClick,
            functionChipOnLongClick = functionChipOnLongClick,
            displayMode = displayMode,
            maxLinesHorizontal = maxLinesHorizontal,
            chipMaxWidth = chipMaxWidth,
            chipFixedWidth = chipFixedWidth,
            useAdaptiveWidth = useAdaptiveWidth,
            useActivityColorForText = useActivityColorForText,
            multiSelect = multiSelect,
            onContentColor = onContentColor,
            labelWidth = labelWidth,
            functionChipSpacing = functionChipSpacing,
        )
    }
}

@Composable
private fun VerticalGridLayout(
    modifier: Modifier,
    chips: List<ChipItem>,
    onChipClick: (Long) -> Unit,
    selectedId: Long?,
    selectedIds: Set<Long>,
    functionChipLabel: String,
    functionChipIcon: @Composable (() -> Unit)?,
    functionChipOnClick: () -> Unit,
    functionChipOnLongClick: () -> Unit,
    displayMode: ChipDisplayMode,
    maxLinesPerColumn: Int,
    chipMaxWidth: Dp,
    chipFixedWidth: Dp,
    useAdaptiveWidth: Boolean,
    useActivityColorForText: Boolean,
    multiSelect: Boolean,
    onContentColor: Color,
    labelWidth: Dp,
    functionChipSpacing: Dp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        FunctionChip(
            label = functionChipLabel,
            icon = functionChipIcon,
            containerColor = Color.Transparent,
            contentColor = onContentColor,
            borderColor = Color.Transparent,
            onClick = functionChipOnClick,
            onLongClick = functionChipOnLongClick,
            modifier = Modifier.width(labelWidth),
        )

        Spacer(modifier = Modifier.width(functionChipSpacing))

        HorizontalScrollView(
            modifier = Modifier.weight(1f),
        ) {
            StaggeredHorizontalGrid(
                maxLines = maxLinesPerColumn,
                horizontalSpacing = 4.dp,
                verticalSpacing = 4.dp,
            ) {
                chips.forEach { chip -> key(chip.id) {
                    AdaptiveActivityChip(
                        chip = chip,
                        displayMode = displayMode,
                        isSelected = if (multiSelect) chip.id in selectedIds else chip.id == selectedId,
                        onClick = { onChipClick(chip.id) },
                        fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                        maxWidth = chipMaxWidth,
                        useActivityColorForText = useActivityColorForText,
                    )
                } }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HorizontalGridLayout(
    modifier: Modifier,
    chips: List<ChipItem>,
    onChipClick: (Long) -> Unit,
    selectedId: Long?,
    selectedIds: Set<Long>,
    functionChipLabel: String,
    functionChipIcon: @Composable (() -> Unit)?,
    functionChipOnClick: () -> Unit,
    functionChipOnLongClick: () -> Unit,
    displayMode: ChipDisplayMode,
    maxLinesHorizontal: Int,
    chipMaxWidth: Dp,
    chipFixedWidth: Dp,
    useAdaptiveWidth: Boolean,
    useActivityColorForText: Boolean,
    multiSelect: Boolean,
    onContentColor: Color,
    labelWidth: Dp,
    functionChipSpacing: Dp,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        FunctionChip(
            label = functionChipLabel,
            icon = functionChipIcon,
            containerColor = Color.Transparent,
            contentColor = onContentColor,
            borderColor = Color.Transparent,
            onClick = functionChipOnClick,
            onLongClick = functionChipOnLongClick,
            modifier = Modifier.width(labelWidth),
        )

        Spacer(modifier = Modifier.width(functionChipSpacing))

        if (maxLinesHorizontal == UNLIMITED_COUNT) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                chips.forEach { chip -> key(chip.id) {
                    AdaptiveActivityChip(
                        chip = chip,
                        displayMode = displayMode,
                        isSelected = if (multiSelect) chip.id in selectedIds else chip.id == selectedId,
                        onClick = { onChipClick(chip.id) },
                        fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                        maxWidth = chipMaxWidth,
                        useActivityColorForText = useActivityColorForText,
                    )
                } }
            }
        } else {
            val lineHeightDp = 28.dp
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(max = lineHeightDp * maxLinesHorizontal)
                    .verticalScroll(rememberScrollState()),
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp,start = 4.dp,bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    chips.forEach { chip -> key(chip.id) {
                        AdaptiveActivityChip(
                            chip = chip,
                            displayMode = displayMode,
                            isSelected = if (multiSelect) chip.id in selectedIds else chip.id == selectedId,
                            onClick = { onChipClick(chip.id) },
                            fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                            maxWidth = chipMaxWidth,
                            useActivityColorForText = useActivityColorForText,
                        )
                    } }
                }
            }
        }
    }
}
