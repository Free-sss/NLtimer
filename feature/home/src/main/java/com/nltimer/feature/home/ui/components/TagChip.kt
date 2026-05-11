package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.component.AppTagChip
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
    AppTagChip(
        label = tag.name,
        color = tag.color,
        modifier = modifier,
    )
}

@Composable
fun TagChipSmall(
    name: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "#$name",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
