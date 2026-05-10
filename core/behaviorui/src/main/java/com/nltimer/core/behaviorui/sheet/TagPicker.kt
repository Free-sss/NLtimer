package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.theme.styledAlpha

/**
 * 标签多选选择器 Composable。
 * 标签以 FlowRow 排列，选中时高亮显示颜色边框。
 *
 * @param tags 所有可选的标签
 * @param selectedTagIds 当前选中的标签 ID 集合
 * @param onTagToggle 切换标签选中状态回调
 * @param modifier 修饰符
 */
@Composable
fun TagPicker(
    tags: List<Tag>,
    selectedTagIds: Set<Long>,
    onTagToggle: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag -> key(tag.id) {
            // 选中状态决定背景色、边框色和文字色
            val isSelected = tag.id in selectedTagIds
            val tagColor = tag.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary
            val backgroundColor = if (isSelected) {
                tagColor.copy(alpha = styledAlpha(0.2f))
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val borderColor = if (isSelected) tagColor else Color.Transparent
            val textColor = if (isSelected) tagColor else MaterialTheme.colorScheme.onSurfaceVariant

            Surface(
                onClick = { onTagToggle(tag.id) },
                shape = RoundedCornerShape(14.dp),
                color = backgroundColor,
                border = if (isSelected) BorderStroke(1.dp, borderColor) else null,
            ) {
                Text(
                    text = tag.name,
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        } }
    }
}
