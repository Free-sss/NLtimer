package com.nltimer.feature.management_activities.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.designsystem.icon.IconRenderer
import com.nltimer.core.designsystem.theme.appAssistChipBorder

/**
 * 活动标签组件
 *
 * 以 Chip 样式展示单个活动，支持单击查看详情和长按移动分组。
 *
 * @param activity 活动数据
 * @param onClick 单击回调
 * @param onLongClick 长按回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivityChip(
    activity: Activity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconRenderer(
                    iconKey = activity.iconKey?.takeIf { it.isNotBlank() },
                    defaultEmoji = "📌",
                    iconSize = 16.dp,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        border = appAssistChipBorder(),
        // 使用 combinedClickable 同时支持单击和长按
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
    )
}
