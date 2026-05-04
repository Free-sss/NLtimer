package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.feature.home.model.TagUiState

/**
 * 标签条 Composable，使用标签颜色作为背景。
 * 单行显示标签名称，超出省略。
 *
 * @param tag 标签 UI 状态
 * @param modifier 修饰符
 */
@Composable
fun TagChip(
    tag: TagUiState,
    modifier: Modifier = Modifier,
) {
    val chipColor = tag.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primaryContainer
    val textColor = if (tag.color != null) {
        if (Color(tag.color).luminance() > 0.5f) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Text(
        text = tag.name,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .background(chipColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 1.dp),
    )
}
