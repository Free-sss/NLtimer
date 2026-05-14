package com.nltimer.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun styledCorner(baseDp: Int): Dp {
    val scale = LocalTheme.current.style.effectiveCornerScale()
    return (baseDp * scale).coerceIn(0f, ShapeTokens.CORNER_SUPER_LARGE.toFloat()).dp
}

@Composable
fun styledBorder(baseDp: Int): Dp {
    val scale = LocalTheme.current.style.effectiveBorderScale()
    return (baseDp * scale).coerceIn(0f, 8f).dp
}

@Composable
fun styledAlpha(baseAlpha: Float): Float {
    val scale = LocalTheme.current.style.effectiveAlphaScale()
    return (baseAlpha * scale).coerceIn(0f, 1f)
}
