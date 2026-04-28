package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.appBorder
import com.nltimer.core.designsystem.theme.toDisplayString
import com.nltimer.feature.home.model.GridCellUiState
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimelineReverseView(
    cells: List<GridCellUiState>,
    onAddClick: () -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    var showLayoutMenu by remember { mutableStateOf(false) }
    
    // Process behaviors and insert idle gaps
    val behaviors = cells.filter { it.behaviorId != null }
        .sortedBy { it.startTime } // Sort ascending for gap processing
    
    val items = mutableListOf<TimelineItemData>()
    
    if (behaviors.isNotEmpty()) {
        // Add current "Idle" if latest behavior is not active
        val latest = behaviors.last()
        if (latest.status != com.nltimer.core.data.model.BehaviorNature.ACTIVE && latest.endTime != null) {
            val now = LocalTime.now()
            if (now.isAfter(latest.endTime)) {
                items.add(TimelineItemData.Idle(latest.endTime, now))
            }
        }
        
        // Add behaviors and gaps in reverse order
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showLayoutMenu = true }
                    ) {
                        Text(
                            text = "时间轴",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showLayoutMenu,
                        onDismissRequest = { showLayoutMenu = false }
                    ) {
                        HomeLayout.values().forEach { layout ->
                            DropdownMenuItem(
                                text = { Text(layout.toDisplayString()) },
                                onClick = {
                                    onLayoutChange(layout)
                                    showLayoutMenu = false
                                }
                            )
                        }
                    }
                }
            }

            items(items) { item ->
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
                            onAddClick = onAddClick
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddClick,
            containerColor = Color(0xFF00C853), // Vivid green
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
        }
    }
}

sealed class TimelineItemData {
    data class Behavior(val behavior: GridCellUiState) : TimelineItemData()
    data class Idle(val start: LocalTime, val end: LocalTime) : TimelineItemData()
}

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
                    tint = Color(0xFF4CAF50).copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

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
        // Left: Time
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

        // Right: Card
        val cardBackground = if (behavior.isCurrent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBackground)
                .appBorder(
                    borderProducer = {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
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
                
                // Duration
                val duration = behavior.durationMs ?: ((behavior.actualDuration ?: 0L) * 1000)
                if (duration > 0) {
                    Text(
                        text = "⏱ ${formatDuration(duration)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }


            }

            if (behavior.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    behavior.tags.forEach { tag ->
                        TagChipSmall(tag.name)
                    }
                }
            }

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

private fun formatDuration(ms: Long): String {
    val hours = ms / 3600000
    val minutes = (ms % 3600000) / 60000
    val seconds = (ms % 60000) / 1000
    return buildString {
        if (hours > 0) append("${hours}时")
        if (minutes > 0 || hours > 0) append("${minutes}分")
        if (hours == 0L) append("${seconds}秒")
    }
}

@Composable
private fun TagChipSmall(name: String) {
    Box(
        modifier = Modifier
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
