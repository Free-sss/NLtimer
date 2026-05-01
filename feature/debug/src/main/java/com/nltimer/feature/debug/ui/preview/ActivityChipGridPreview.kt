package com.nltimer.feature.debug.ui.preview

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
import androidx.compose.ui.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.designsystem.theme.NLtimerTheme
import kotlin.math.max
import kotlin.math.min

@Composable
private fun HorizontalScrollView(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.horizontalScroll(rememberScrollState()),
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

enum class ChipDisplayMode {
    None,
    Filled,
    Underline,
    Capsules,
    RoundedCorners,
    Squares,
    SquareBorder,
    HandDrawn,
    DashedLines,
}

enum class GridLayoutMode {
    Horizontal,
    Vertical,
}

data class ActivityChipData(
    val name: String,
    val color: Color
)

@Preview(showBackground = true)
@Composable
fun ActivityChipGridDebugPreview() {
    val sampleActivities = listOf(
        ActivityChipData("学习", Color(0xFF1B5E20)),
        ActivityChipData("读书", Color(0xFF43A047)),
        ActivityChipData("英语", Color(0xFF66BB6A)),
        ActivityChipData("c语言", Color(0xFF81C784)),
        ActivityChipData("微积分", Color(0xFF757575)),
        ActivityChipData("休息", Color(0xFF212121)),
        ActivityChipData("睡觉", Color(0xFF00BFA5)),
        ActivityChipData("Take a nap", Color(0xFF4DB6AC)),
        ActivityChipData("冥想", Color(0xFF006064)),
        ActivityChipData("信息流", Color(0xFFB71C1C)),
        ActivityChipData("生活", Color(0xFF8D6E63)),
        ActivityChipData("厕所", Color(0xFFD7CCC8)),
        ActivityChipData("金刚功", Color(0xFF795548)),
        ActivityChipData("吃饭", Color(0xFFD2B48C)),
        ActivityChipData("多巴胺", Color(0xFFB8860B))
    )


    NLtimerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("自适应宽度 (Adaptive)", Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                ActivityGridComponent(
                    activities = sampleActivities,
                    onActivityClick = { },
                    functionChipOnClick = { },
                    useAdaptiveWidth = true,
                    layoutMode = GridLayoutMode.Vertical
                )

                Text("固定宽度 (Fixed 80dp)", Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                ActivityGridComponent(
                    activities = sampleActivities,
                    onActivityClick = { },
                    functionChipOnClick = { },
                    useAdaptiveWidth = false,
                    chipFixedWidth = 80.dp,
                    layoutMode = GridLayoutMode.Vertical
                )
                
                Text("Horizontal Flow (Adaptive)", Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                ActivityGridComponent(
                    activities = sampleActivities,
                    onActivityClick = { },
                    functionChipOnClick = { },
                    useAdaptiveWidth = true,
                    layoutMode = GridLayoutMode.Horizontal
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActivityGridComponent(
    modifier: Modifier = Modifier,
    activities: List<ActivityChipData>,
    onActivityClick: (ActivityChipData) -> Unit,
    functionChipLabel: String = "活动管理",
    functionChipIcon: @Composable (() -> Unit)? = null,
    functionChipOnClick: () -> Unit,
    displayMode: ChipDisplayMode = ChipDisplayMode.Filled,
    layoutMode: GridLayoutMode = GridLayoutMode.Horizontal,
    maxLinesPerColumn: Int = 2,
    maxLinesHorizontal: Int = 2,
    useAdaptiveWidth: Boolean = true,
    chipMaxWidth: Dp = 120.dp,
    chipFixedWidth: Dp = 80.dp,
    sortOrder: List<String> = emptyList(),
) {
    val defaultIcon: @Composable () -> Unit = {
        Icon(
            Icons.Default.Settings,
            contentDescription = "管理",
            modifier = Modifier.size(14.dp),
        )
    }

    val labelWidth = 64.dp
    val functionChipSpacing = 4.dp

    if (layoutMode == GridLayoutMode.Vertical) {
        val sortedActivities = if (sortOrder.isEmpty()) {
            activities
        } else {
            activities.sortedWith { a, b ->
                val aPriority = sortOrder.indexOfFirst { a.name.contains(it, ignoreCase = true) }
                    .let { if (it == -1) sortOrder.size else it }
                val bPriority = sortOrder.indexOfFirst { b.name.contains(it, ignoreCase = true) }
                    .let { if (it == -1) sortOrder.size else it }
                aPriority.compareTo(bPriority)
            }
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            FunctionChip(
                label = functionChipLabel,
                icon = functionChipIcon ?: defaultIcon,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF616161).copy(alpha = 0.9f),
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
                    sortedActivities.forEach { activity ->
                        AdaptiveActivityChip(
                            activity = activity,
                            displayMode = displayMode,
                            onClick = { onActivityClick(activity) },
                            fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                            maxWidth = chipMaxWidth
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
                icon = functionChipIcon ?: defaultIcon,
                containerColor = Color.Transparent,
                contentColor = Color(0xFF616161).copy(alpha = 0.9f),
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
                    activities.forEach { activity ->
                        AdaptiveActivityChip(
                            activity = activity,
                            displayMode = displayMode,
                            onClick = { onActivityClick(activity) },
                            fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                            maxWidth = chipMaxWidth
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
                        activities.forEach { activity ->
                            AdaptiveActivityChip(
                                activity = activity,
                                displayMode = displayMode,
                                onClick = { onActivityClick(activity) },
                                fixedWidth = if (useAdaptiveWidth) null else chipFixedWidth,
                                maxWidth = chipMaxWidth
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdaptiveActivityChip(
    activity: ActivityChipData,
    displayMode: ChipDisplayMode,
    onClick: () -> Unit,
    fixedWidth: Dp? = null,
    maxWidth: Dp = 120.dp,
) {
    val baseColor = activity.color
    val containerColor = baseColor.copy(alpha = 0.15f)
    val contentColor = baseColor.copy(alpha = 0.9f)
    val borderColor = baseColor.copy(alpha = 0.5f)

    val shape = when (displayMode) {
        ChipDisplayMode.Capsules -> RoundedCornerShape(50)
        ChipDisplayMode.Squares -> RoundedCornerShape(0)
        ChipDisplayMode.SquareBorder -> RoundedCornerShape(0)
        ChipDisplayMode.None -> RoundedCornerShape(0)
        else -> RoundedCornerShape(6.dp)
    }

    val surfaceColor = when (displayMode) {
        ChipDisplayMode.Filled, ChipDisplayMode.Capsules,
        ChipDisplayMode.RoundedCorners, ChipDisplayMode.Squares -> containerColor
        else -> Color.Transparent
    }

    val border = when (displayMode) {
        ChipDisplayMode.Capsules -> BorderStroke(1.dp, borderColor)
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
                                drawLine(
                                    color = containerColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = sw,
                                )
                            }
                        }
                        ChipDisplayMode.SquareBorder -> {
                            Modifier.drawBehind {
                                val sw = 1.5.dp.toPx()
                                drawRect(
                                    color = borderColor,
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
                                val paint = Paint().apply {
                                    isAntiAlias = true
                                    style = Paint.Style.STROKE
                                    strokeWidth = sw
                                    color = android.graphics.Color.argb(
                                        (borderColor.alpha * 255).toInt(),
                                        (borderColor.red * 255).toInt(),
                                        (borderColor.green * 255).toInt(),
                                        (borderColor.blue * 255).toInt(),
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
                                drawRoundRect(
                                    color = borderColor,
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
                    text = activity.name,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FunctionChip(
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
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(16.dp)) { icon() }
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

@Preview(showBackground = true)
@Composable
fun ActivityChipPreview() {
    NLtimerTheme {
        AdaptiveActivityChip(
            activity = ActivityChipData("学习", Color(0xFF1B5E20)),
            displayMode = ChipDisplayMode.Filled,
            onClick = { }
        )
    }
}
