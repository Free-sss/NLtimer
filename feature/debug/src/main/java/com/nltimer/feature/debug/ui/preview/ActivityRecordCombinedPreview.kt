package com.nltimer.feature.debug.ui.preview

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime

@Preview(showBackground = true)
@Composable
fun ActivityRecordCombinedPreview() {
    var showSheet by remember { mutableStateOf(false) }
    var baseTime by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedMode by remember { mutableStateOf(ChipDisplayMode.Filled) }
    var modeMenuExpanded by remember { mutableStateOf(false) }
    var selectedLayout by remember { mutableStateOf(GridLayoutMode.Horizontal) }
    var layoutMenuExpanded by remember { mutableStateOf(false) }
    var selectedMaxLines by remember { mutableStateOf(2) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "活动标签样式模式",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        Surface(
                            onClick = { modeMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = selectedMode.name,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = modeMenuExpanded,
                            onDismissRequest = { modeMenuExpanded = false },
                        ) {
                            ChipDisplayMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.name,
                                            fontWeight = if (mode == selectedMode) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        selectedMode = mode
                                        modeMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedMode) {
                            ChipDisplayMode.None -> "无样式"
                            ChipDisplayMode.Filled -> "填充背景"
                            ChipDisplayMode.Underline -> "底部下划线"
                            ChipDisplayMode.Capsules -> "胶囊边框"
                            ChipDisplayMode.RoundedCorners -> "圆角背景"
                            ChipDisplayMode.Squares -> "直角背景"
                            ChipDisplayMode.SquareBorder -> "方形边框"
                            ChipDisplayMode.HandDrawn -> "手绘风格"
                            ChipDisplayMode.DashedLines -> "虚线边框"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "网格布局模式",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        Surface(
                            onClick = { layoutMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = selectedLayout.name,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = layoutMenuExpanded,
                            onDismissRequest = { layoutMenuExpanded = false },
                        ) {
                            GridLayoutMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (mode) {
                                                GridLayoutMode.Horizontal -> "Horizontal (横向流)"
                                                GridLayoutMode.Vertical -> "Vertical (纵向流)"
                                            },
                                            fontWeight = if (mode == selectedLayout) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        selectedLayout = mode
                                        layoutMenuExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedLayout) {
                            GridLayoutMode.Horizontal -> "水平排列，超出换行"
                            GridLayoutMode.Vertical -> "上下排列，满列换列，支持横滑"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )

                    if (selectedLayout == GridLayoutMode.Vertical) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "每列最多行数",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                onClick = { if (selectedMaxLines > 1) selectedMaxLines-- },
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    text = "\u2212",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                            }
                            Text(
                                text = "$selectedMaxLines",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            Surface(
                                onClick = { if (selectedMaxLines < 10) selectedMaxLines++ },
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            baseTime = LocalDateTime.now()
                            showSheet = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) { 
                        Text("打开活动记录组合弹窗")
                    }
                }
            }
        }
    }

    if (showSheet) {
        ActivityRecordCombinedSheet(
            baseTime = baseTime,
            displayMode = selectedMode,
            layoutMode = selectedLayout,
            columnLines = selectedMaxLines,
            onDismiss = { showSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityRecordCombinedSheet(
    baseTime: LocalDateTime,
    displayMode: ChipDisplayMode,
    layoutMode: GridLayoutMode,
    columnLines: Int,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val fakeActivities = listOf(
        ActivityChipData("学习", Color(0xFF1B5E20)),
        ActivityChipData("读书", Color(0xFF43A047)),
        ActivityChipData("英语", Color(0xFF66BB6A)),
        ActivityChipData("微积分", Color(0xFF757575)),
        ActivityChipData("休息", Color(0xFF212121)),
        ActivityChipData("睡觉", Color(0xFF00BFA5)),
        ActivityChipData("冥想", Color(0xFF006064)),
        ActivityChipData("信息流", Color(0xFFB71C1C)),
        ActivityChipData("生活", Color(0xFF8D6E63)),
        ActivityChipData("吃饭", Color(0xFFD2B48C)),
        ActivityChipData("多巴胺", Color(0xFFB8860B)),
        ActivityChipData("运动", Color(0xFFFF7043)),
    )
    val sampleTags = listOf(
        ActivityChipData("标签1", Color(0xFF1B5E20)),
        ActivityChipData("标签123", Color(0xFF43A047)),
        ActivityChipData("标签123456", Color(0xFF66BB6A)),
        ActivityChipData("标签123456789", Color(0xFF81C784)),
        ActivityChipData("⌚标", Color(0xFF757575)),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Text(
                text = "活动记录",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.padding(top = 12.dp,start = 24.dp).fillMaxWidth(1f),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
                .animateContentSize()
        ) {
            CombinedTimeAdjustment()
            DualTimePicker(baseTime = baseTime)
            CombinedTimeAdjustment()
            Spacer(modifier = Modifier.height(16.dp))

            ActivityGridComponent(
                modifier = Modifier.padding(start = 10.dp),
                activities = fakeActivities,
                onActivityClick = { },
                functionChipLabel = "活动",
                functionChipOnClick = { },
                displayMode = displayMode,
                layoutMode = layoutMode,
                maxLinesPerColumn = columnLines,
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActivityGridComponent(
                modifier = Modifier.padding(start = 10.dp),
                activities = sampleTags,
                onActivityClick = { },
                functionChipLabel = "标签",
                functionChipOnClick = { },
                displayMode = displayMode,
                layoutMode = layoutMode,
                maxLinesPerColumn = columnLines,
            )
            ActivityNoteComponent(
                onHistoryClick = { },
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                ) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                ) {
                    Text("确认", fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun CombinedTimeAdjustment() {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeAdjustmentComponent(
            currentTime = currentTime,
            onTimeChanged = { currentTime = it },
        )
    }
}
