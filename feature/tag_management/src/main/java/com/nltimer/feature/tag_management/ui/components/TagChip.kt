package com.nltimer.feature.tag_management.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.nltimer.core.data.model.Tag

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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 解析标签背景色，无自定义色时使用主题 surfaceVariant
    val backgroundColor = tag.color?.let { Color(it) }
        ?: MaterialTheme.colorScheme.surfaceVariant

    // 解析标签文字色，无自定义色时使用主题 onSurfaceVariant
    val contentColor = tag.textColor?.let { Color(it) }
        ?: MaterialTheme.colorScheme.onSurfaceVariant

    // 使用 AssistChip 并添加 combinedClickable 以支持长按
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
            )
        },
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = backgroundColor,
            labelColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledLabelColor = contentColor.copy(alpha = 0.5f),
        ),
    )
}
