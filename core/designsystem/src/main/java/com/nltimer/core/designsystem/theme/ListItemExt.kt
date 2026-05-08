package com.nltimer.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

@Composable
fun listItemColors(): ListItemColors =
    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)

@Composable
fun leadingItemShape(): Shape =
    RoundedCornerShape(
        topStart = styledCorner(ShapeTokens.CORNER_LARGE),
        topEnd = styledCorner(ShapeTokens.CORNER_LARGE),
        bottomEnd = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        bottomStart = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
    )

@Composable
fun middleItemShape(): Shape =
    RoundedCornerShape(
        topStart = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        topEnd = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        bottomStart = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        bottomEnd = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
    )

@Composable
fun endItemShape(): Shape =
    RoundedCornerShape(
        topStart = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        topEnd = styledCorner(ShapeTokens.CORNER_EXTRA_SMALL),
        bottomEnd = styledCorner(ShapeTokens.CORNER_LARGE),
        bottomStart = styledCorner(ShapeTokens.CORNER_LARGE),
    )

@Composable
fun detachedItemShape(): Shape = RoundedCornerShape(styledCorner(ShapeTokens.CORNER_LARGE))
