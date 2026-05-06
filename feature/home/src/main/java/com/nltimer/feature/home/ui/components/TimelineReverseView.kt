package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.util.formatDuration
import com.nltimer.core.data.util.hhmmFormatter
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.feature.home.model.GridCellUiState
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 时间线倒序视图 Composable。
 * 将行为按时间排序后倒序展示，自动插入空闲时间段，支持布局切换。
 *
 * @param cells 所有单元格数据
 * @param onAddClick 添加行为回调
 * @param onLayoutChange 切换布局模式回调
 * @param modifier 修饰符
 */
@Composable
fun TimelineReverseView(
    cells: List<GridCellUiState>,
    onAddClick: (idleStart: LocalTime?, idleEnd: LocalTime?) -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = hhmmFormatter

    val behaviors = remember(cells) {
        cells.filter { it.behaviorId != null }
            .sortedBy { it.startTime }
    }
    
    val timelineItems = remember(behaviors) {
        val items = mutableListOf<TimelineItemData>()
        
        if (behaviors.isNotEmpty()) {
            val latest = behaviors.last()
            if (latest.status != BehaviorNature.ACTIVE && latest.endTime != null) {
                val now = LocalTime.now()
                if (now.isAfter(latest.endTime)) {
                    items.add(TimelineItemData.Idle(latest.endTime, now))
                }
            }
            
            for (i in behaviors.indices.reversed()) {
                val behavior = behaviors[i]
                items.add(TimelineItemData.Behavior(behavior))
                
                if (i > 0) {
                    val prevEnd = behaviors[i-1].endTime
                    val currentStart = behavior.startTime
                    if (prevEnd != null && currentStart != null && currentStart.isAfter(prevEnd)) {
                        items.add(TimelineItemData.Idle(prevEnd, currentStart))
                    }
                }
            }
        }
        items
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                LayoutMenuHeader(
                    title = "时间轴",
                    onLayoutChange = onLayoutChange,
                )
            }

            items(timelineItems) { item ->
                when (item) {
                    is TimelineItemData.Behavior -> {
                        TimelineBehaviorItem(
                            behavior = item.behavior,
                            timeFormatter = timeFormatter
                        )
                    }
                    is TimelineItemData.Idle -> {
                        TimelineIdleItem(
                            start = item.start,
                            end = item.end,
                            timeFormatter = timeFormatter,
                            onAddClick = { onAddClick(item.start, item.end) }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * 时间线列表项数据密封类。
 * 包含行为项和空闲时间段项两种类型。
 */
sealed class TimelineItemData {
    data class Behavior(val behavior: GridCellUiState) : TimelineItemData()
    data class Idle(val start: LocalTime, val end: LocalTime) : TimelineItemData()
}

/**
 * 空闲时间段条目 Composable，显示起止时间和空闲时长。
 */
@Composable
private fun TimelineIdleItem(
    start: LocalTime,
    end: LocalTime,
    timeFormatter: DateTimeFormatter,
    onAddClick: () -> Unit
) {
    val duration = Duration.between(start, end)
    val durationText = formatDuration(duration.toMillis())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = end.format(timeFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = start.format(timeFormatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                .appBorder(
                    borderProducer = {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "❓ 空闲 : $durationText",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 时间线行为条目 Composable，左侧显示时间，右侧显示行为卡片。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineBehaviorItem(
    behavior: GridCellUiState,
    timeFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左侧：显示开始和结束时间
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = behavior.startTime?.format(timeFormatter) ?: "",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            behavior.endTime?.let {
                Text(
                    text = it.format(timeFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 右侧：行为卡片，包含名称、时长、标签和备注
        val cardBackground = if (behavior.isCurrent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        }
        val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

        Column(
            modifier = Modifier
                .weight(1f)
                .behaviorCardStyle(cardBackground, borderColor)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${behavior.activityEmoji ?: "❓"} ${behavior.activityName ?: "未知"}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                // 计算并显示时长，优先使用 durationMs
                val duration = behavior.durationMs ?: ((behavior.actualDuration ?: 0L) * 1000)
                if (duration > 0) {
                    Text(
                        text = "⏱ ${formatDuration(duration)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


            }

            BehaviorTagRow(behavior.tags)

            behavior.note?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


