package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 浮动时间标签 Composable。
 * 当前行使用 tertiary 色，其余行使用 primary 色。
 *
 * @param time 时间值
 * @param isCurrentRow 是否为当前行
 * @param modifier 修饰符
 */
@Composable
fun TimeFloatingLabel(
    time: LocalTime,
    isCurrentRow: Boolean,
    modifier: Modifier = Modifier,
) {
    // 根据是否当前行选择背景色和文字色
    val backgroundColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (isCurrentRow) {
        MaterialTheme.colorScheme.onTertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Text(
        text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
        color = contentColor,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp),
    )
}
