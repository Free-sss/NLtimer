package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.feature.home.model.GridCellUiState

/**
 * 网格视图中的行为单元格 Composable。
 * 根据行为状态渲染不同的背景色、边框色和白金成就效果。
 *
 * @param cell 单元格 UI 状态数据
 * @param modifier 修饰符
 */
@Composable
fun GridCell(
    cell: GridCellUiState,
    modifier: Modifier = Modifier,
) {
    // 判断是否为已完成且达到白金成就等级的行为
    val isPlatinum = cell.wasPlanned && cell.status == BehaviorNature.COMPLETED
    val platinumStrength = cell.achievementLevel?.let { it / 100f } ?: 0f

    // 根据活跃/白金/普通状态选择背景色
    val backgroundColor = when {
        cell.isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        isPlatinum -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .heightIn(max = 140.dp)
            .clipToBounds()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .appBorder(
                borderProducer = {
                    val borderColor = when {
                        isPlatinum -> {
                            val strength = platinumStrength
                            Color(
                                red = (0.78f + 0.22f * strength),
                                green = (0.69f + 0.31f * strength),
                                blue = 1.0f,
                                alpha = 0.5f + 0.5f * strength,
                            )
                        }
                        cell.isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                    val borderWidth = if (cell.isCurrent || isPlatinum) 2.dp else 1.dp
                    BorderStroke(borderWidth, borderColor)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
            ,
            
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        Row(){
            cell.activityIconKey?.let { iconKey ->
                Text(text = iconKey, style = MaterialTheme.typography.bodyMedium)
            }

            // 显示活动名称（单行省略）
            cell.activityName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        // 渲染标签列表为 FlowRow 标签条（整体缩小 10%）
        if (cell.tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.scale(0.8f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                maxLines = 2,
            ) {
                cell.tags.forEach { tag -> TagChip(tag = tag) }
            }
        }
        cell.note?.let { note ->
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
