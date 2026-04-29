package com.nltimer.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** 列表项中连接处圆角半径（与相邻项相接边） */
private const val CONNECTED_CORNER_RADIUS = 4

/** 列表项端部圆角半径（首项顶边/末项底边） */
private const val END_CORNER_RADIUS = 16

/**
 * 获取列表项颜色配置
 * 使用 surfaceContainerHigh 作为背景色，使列表项在层级上略高于页面底色
 */
@Composable
fun listItemColors(): ListItemColors =
    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)

/** 列表首项的圆角形状：顶部大圆角，底部小圆角（与下一项相连） */
fun leadingItemShape(): Shape =
    RoundedCornerShape(
        topStart = END_CORNER_RADIUS.dp,
        topEnd = END_CORNER_RADIUS.dp,
        bottomEnd = CONNECTED_CORNER_RADIUS.dp,
        bottomStart = CONNECTED_CORNER_RADIUS.dp,
    )

/** 列表中间项的圆角形状：四角均用小圆角，与上下项平滑衔接 */
fun middleItemShape(): Shape =
    RoundedCornerShape(
        topStart = CONNECTED_CORNER_RADIUS.dp,
        topEnd = CONNECTED_CORNER_RADIUS.dp,
        bottomStart = CONNECTED_CORNER_RADIUS.dp,
        bottomEnd = CONNECTED_CORNER_RADIUS.dp,
    )

/** 列表末项的圆角形状：底部大圆角，顶部小圆角（与上一项相连） */
fun endItemShape(): Shape =
    RoundedCornerShape(
        topStart = CONNECTED_CORNER_RADIUS.dp,
        topEnd = CONNECTED_CORNER_RADIUS.dp,
        bottomEnd = END_CORNER_RADIUS.dp,
        bottomStart = END_CORNER_RADIUS.dp,
    )

/** 独立列表项的圆角形状：四角均用大圆角，不与任何项相连 */
fun detachedItemShape(): Shape = RoundedCornerShape(END_CORNER_RADIUS.dp)
