package com.nltimer.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.IconContainerSize

fun Modifier.iconContainer(size: IconContainerSize, color: Color): Modifier = when (size) {
    IconContainerSize.NONE -> this
    IconContainerSize.CIRCLE_SMALL -> this.size(36.dp).clip(CircleShape).background(color)
    IconContainerSize.CIRCLE_LARGE -> this.size(44.dp).clip(CircleShape).background(color)
}
