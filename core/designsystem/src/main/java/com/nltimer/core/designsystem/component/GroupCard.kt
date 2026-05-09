package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.nltimer.core.designsystem.theme.ShapeTokens
import com.nltimer.core.designsystem.theme.styledCorner

@Composable
fun GroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_LARGE)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
fun groupItemShape(index: Int, listSize: Int): Shape {
    return when {
        listSize == 1 -> RoundedCornerShape(styledCorner(ShapeTokens.CORNER_EXTRA_LARGE))
        index == 0 -> RoundedCornerShape(
            topStart = styledCorner(ShapeTokens.CORNER_EXTRA_LARGE),
            topEnd = styledCorner(ShapeTokens.CORNER_EXTRA_LARGE),
            bottomEnd = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
            bottomStart = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        )
        index == listSize - 1 -> RoundedCornerShape(
            topStart = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
            topEnd = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
            bottomEnd = styledCorner(ShapeTokens.CORNER_EXTRA_LARGE),
            bottomStart = styledCorner(ShapeTokens.CORNER_EXTRA_LARGE),
        )
        else -> RoundedCornerShape(styledCorner(ShapeTokens.CORNER_EXTRA_SMALL))
    }
}
