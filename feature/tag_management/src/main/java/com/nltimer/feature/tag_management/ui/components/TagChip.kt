package com.nltimer.feature.tag_management.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.component.AppTagChip
import com.nltimer.core.designsystem.component.AppTagChipStyle

/**
 * 标签 Chip 组件
 *
 * 显示一个带颜色的标签，支持点击和长按操作。
 *
 * @param tag 标签数据，包含名称、背景色、文字色
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param modifier 修饰符
 */
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppTagChip(
        label = tag.name,
        color = tag.color,
        prefixed = true,
        style = AppTagChipStyle.Assist,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    )
}
