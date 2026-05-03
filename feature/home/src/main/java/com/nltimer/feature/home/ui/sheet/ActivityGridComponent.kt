package com.nltimer.feature.home.ui.sheet

import android.graphics.DiscretePathEffect as AndroidDiscretePathEffect
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.GridLayoutMode
import kotlin.math.max

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

@Composable
private fun HorizontalScrollView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 4.dp),
    ) {
        content()
    }
}

@Composable
fun StaggeredHorizontalGrid(
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    horizontalSpacing: Dp = 4.dp,
    verticalSpacing: Dp = 4.dp,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            return@Layout layout(0, 0) {}
        }

        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0))
        }

        val rowWidths = IntArray(maxLines) { 0 }
        val rowHeights = IntArray(maxLines) { 0 }
        val rowAssignments = List(maxLines) { mutableListOf<Int>() }

        placeables.forEachIndexed { index, placeable ->
            var targetRow = 0
            var minTargetWidth = rowWidths[0]
            for (row in 1 until maxLines) {
                if (rowWidths[row] < minTargetWidth) {
                    minTargetWidth = rowWidths[row]
                    targetRow = row
                }
            }

            rowAssignments[targetRow].add(index)
            val addedWidth = if (rowAssignments[targetRow].size == 1) {
                placeable.width
            } else {
                placeable.width + horizontalSpacingPx
            }
            rowWidths[targetRow] += addedWidth
            rowHeights[targetRow] = max(rowHeights[targetRow], placeable.height)
        }

        val totalWidth = rowWidths.maxOrNull() ?: 0
        val totalHeight = rowHeights.sum() + verticalSpacingPx * (maxLines - 1)

        layout(totalWidth, totalHeight) {
            var currentY = 0
            for (row in 0 until maxLines) {
                var currentX = 0
                rowAssignments[row].forEach { index ->
                    placeables[index].placeRelative(currentX, currentY)
                    currentX += placeables[index].width + horizontalSpacingPx
                }
                currentY += rowHeights[row] + verticalSpacingPx
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    val onContentColor = MaterialTheme.colorScheme.onSecondaryContainer
    val labelWidth = 64.dp
    val functionChipSpacing = 3.dp

    if (layoutMode == GridLayoutMode.Vertical) {
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
                    chips.forEach { chip ->
                        AdaptiveActivityChip(
                            chip = chip,
                            displayMode = displayMode,
                            isSelected = if (multiSelect) chip.id in selectedIds else chip.id == selectedId,
                            onClick = { onChipClick(chip.id) },
                            fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                            maxWidth = chipMaxWidth,
                            useActivityColorForText = useActivityColorForText,
                        )
                    }
                }
            }
        }
    } else {
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
                modifier = Modifier.width(labelWidth),
            )

            Spacer(modifier = Modifier.width(functionChipSpacing))

            if (maxLinesHorizontal == Int.MAX_VALUE) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    chips.forEach { chip ->
                        AdaptiveActivityChip(
                            chip = chip,
                            displayMode = displayMode,
                            isSelected = if (multiSelect) chip.id in selectedIds else chip.id == selectedId,
                            onClick = { onChipClick(chip.id) },
                            fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                            maxWidth = chipMaxWidth,
                            useActivityColorForText = useActivityColorForText,
                        )
                    }
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        chips.forEach { chip ->
                            AdaptiveActivityChip(
                                chip = chip,
                                displayMode = displayMode,
                                isSelected = if (multiSelect) chip.id in selectedIds else chip.id == selectedId,
                                onClick = { onChipClick(chip.id) },
                                fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                                maxWidth = chipMaxWidth,
                                useActivityColorForText = useActivityColorForText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun AdaptiveActivityChip(
    chip: ChipItem,
    displayMode: ChipDisplayMode,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    fixedWidth: Dp? = null,
    maxWidth: Dp = 120.dp,
    useActivityColorForText: Boolean = true,
) {
    val baseColor = chip.color
    val containerColor = baseColor.copy(alpha = 0.15f)
    val contentColor = if (useActivityColorForText) {
        baseColor.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = baseColor.copy(alpha = 0.5f)
    val selectedBorderColor = MaterialTheme.colorScheme.primary

    val shape = when (displayMode) {
        ChipDisplayMode.Capsules -> RoundedCornerShape(50)
        ChipDisplayMode.Squares -> RoundedCornerShape(0)
        ChipDisplayMode.SquareBorder -> RoundedCornerShape(0)
        ChipDisplayMode.None -> RoundedCornerShape(0)
        else -> RoundedCornerShape(6.dp)
    }

    val surfaceColor = when (displayMode) {
        ChipDisplayMode.Filled, ChipDisplayMode.Capsules,
        ChipDisplayMode.RoundedCorners, ChipDisplayMode.Squares -> if (isSelected) MaterialTheme.colorScheme.primaryContainer else containerColor
        else -> Color.Transparent
    }

    val border = when (displayMode) {
        ChipDisplayMode.Capsules -> BorderStroke(1.dp, if (isSelected) selectedBorderColor else borderColor)
        else -> null
    }

    val chipModifier = if (fixedWidth != null) {
        Modifier.height(24.dp).width(fixedWidth)
    } else {
        Modifier.height(24.dp).widthIn(max = maxWidth)
    }

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Surface(
            onClick = onClick,
            modifier = chipModifier
                .then(
                    when (displayMode) {
                        ChipDisplayMode.Underline -> {
                            Modifier.drawBehind {
                                val sw = 2.dp.toPx()
                                val y = size.height - sw / 2
                                val lineColor = if (isSelected) selectedBorderColor else containerColor
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = sw,
                                )
                            }
                        }
                        ChipDisplayMode.SquareBorder -> {
                            Modifier.drawBehind {
                                val sw = 1.5.dp.toPx()
                                val c = if (isSelected) selectedBorderColor else borderColor
                                drawRect(
                                    color = c,
                                    style = Stroke(
                                        width = sw,
                                        pathEffect = PathEffect.cornerPathEffect(2.5f),
                                        join = StrokeJoin.Round,
                                        cap = StrokeCap.Round,
                                    ),
                                )
                            }
                        }
                        ChipDisplayMode.HandDrawn -> {
                            Modifier.drawBehind {
                                val sw = 1.5.dp.toPx()
                                val c = if (isSelected) selectedBorderColor else borderColor
                                val paint = Paint().apply {
                                    isAntiAlias = true
                                    style = Paint.Style.STROKE
                                    strokeWidth = sw
                                    color = android.graphics.Color.argb(
                                        (c.alpha * 255).toInt(),
                                        (c.red * 255).toInt(),
                                        (c.green * 255).toInt(),
                                        (c.blue * 255).toInt(),
                                    )
                                    strokeJoin = Paint.Join.ROUND
                                    strokeCap = Paint.Cap.ROUND
                                    pathEffect = AndroidDiscretePathEffect(10f, 5f)
                                }
                                drawContext.canvas.nativeCanvas.drawRect(
                                    0f, 0f, size.width, size.height, paint,
                                )
                            }
                        }
                        ChipDisplayMode.DashedLines -> {
                            Modifier.drawBehind {
                                val sw = 1.5.dp.toPx()
                                val r = 6.dp.toPx()
                                val c = if (isSelected) selectedBorderColor else borderColor
                                drawRoundRect(
                                    color = c,
                                    cornerRadius = CornerRadius(r, r),
                                    style = Stroke(
                                        width = sw,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(8f, 4f),
                                        ),
                                    ),
                                )
                            }
                        }
                        else -> Modifier
                    }
                ),
            color = surfaceColor,
            contentColor = contentColor,
            shape = shape,
            border = border,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = if (displayMode == ChipDisplayMode.None) 0.dp else 8.dp),
            ) {
                Text(
                    text = chip.name,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor,
                )
            }
        }
    }
}

@Composable
internal fun FunctionChip(
    modifier: Modifier = Modifier,
    label: String,
    icon: @Composable (() -> Unit)? = null,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(24.dp),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(14.dp)) { icon() }
            }
            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
