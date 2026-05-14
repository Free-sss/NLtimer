package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
internal fun HorizontalScrollView(
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
