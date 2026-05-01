package com.nltimer.feature.debug.ui.preview

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime

data class GridConfig(
    val displayMode: MutableState<ChipDisplayMode>,
    val layoutMode: MutableState<GridLayoutMode>,
    val columnLines: MutableState<Int>,
    val horizontalLines: MutableState<Int>,
    val useActivityColorForText: MutableState<Boolean>,
)

@Preview(showBackground = true)
@Composable
fun ActivityRecordCombinedPreview() {
    var showSheet by remember { mutableStateOf(false) }
    var baseTime by remember { mutableStateOf(LocalDateTime.now()) }

    val activityConfig = remember {
        GridConfig(
            displayMode = mutableStateOf(ChipDisplayMode.Filled),
            layoutMode = mutableStateOf(GridLayoutMode.Horizontal),
            columnLines = mutableStateOf(2),
            horizontalLines = mutableStateOf(2),
            useActivityColorForText = mutableStateOf(true),
        )
    }
    val tagConfig = remember {
        GridConfig(
            displayMode = mutableStateOf(ChipDisplayMode.Filled),
            layoutMode = mutableStateOf(GridLayoutMode.Horizontal),
            columnLines = mutableStateOf(2),
            horizontalLines = mutableStateOf(2),
            useActivityColorForText = mutableStateOf(true),
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GridConfigBlock(label = "活动", config = activityConfig)
            GridConfigBlock(label = "标签", config = tagConfig)

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

    if (showSheet) {
        ActivityRecordCombinedSheet(
            baseTime = baseTime,
            activityConfig = activityConfig,
            tagConfig = tagConfig,
            onDismiss = { showSheet = false },
        )
    }
}

@Composable
private fun GridConfigBlock(
    label: String,
    config: GridConfig,
) {
    var modeExpanded by remember { mutableStateOf(false) }
    var layoutExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$label 配置",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "样式",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Surface(
                            onClick = { modeExpanded = true },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = config.displayMode.value.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = modeExpanded,
                            onDismissRequest = { modeExpanded = false },
                        ) {
                            ChipDisplayMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.name,
                                            fontWeight = if (mode == config.displayMode.value) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        config.displayMode.value = mode
                                        modeExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "布局",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Surface(
                            onClick = { layoutExpanded = true },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = config.layoutMode.value.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = layoutExpanded,
                            onDismissRequest = { layoutExpanded = false },
                        ) {
                            GridLayoutMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.name,
                                            fontWeight = if (mode == config.layoutMode.value) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        config.layoutMode.value = mode
                                        layoutExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (config.layoutMode.value == GridLayoutMode.Vertical) {
                StepperControl(
                    label = "每列行数",
                    value = config.columnLines,
                    min = 1,
                    max = 10,
                )
            }

            if (config.layoutMode.value == GridLayoutMode.Horizontal) {
                StepperControl(
                    label = "最多行数（0=无限）",
                    value = config.horizontalLines,
                    min = 0,
                    max = 10,
                    infiniteAtMin = true,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ToggleControl(
                label = "文字使用活动色",
                checked = config.useActivityColorForText.value,
                onCheckedChange = { config.useActivityColorForText.value = it },
            )
        }
    }
}

@Composable
private fun ToggleControl(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = { onCheckedChange(false) },
            shape = RoundedCornerShape(6.dp),
            color = if (!checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "强调色",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (!checked) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (!checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Surface(
            onClick = { onCheckedChange(true) },
            shape = RoundedCornerShape(6.dp),
            color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "活动色",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun StepperControl(
    label: String,
    value: MutableState<Int>,
    min: Int,
    max: Int,
    infiniteAtMin: Boolean = false,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = { if (value.value > min) value.value-- },
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
            text = if (infiniteAtMin && value.value == min) "\u221E" else "${value.value}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Surface(
            onClick = {
                if (value.value < max) value.value++
                else if (infiniteAtMin) value.value = min
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityRecordCombinedSheet(
    baseTime: LocalDateTime,
    activityConfig: GridConfig,
    tagConfig: GridConfig,
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
        ActivityChipData("\u231A标", Color(0xFF757575)),
    )

    val emphasisColor = MaterialTheme.colorScheme.secondaryContainer

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    val strokeWidthPx = 3.dp.toPx()
                    val halfStroke = strokeWidthPx / 2
                    val r = 28.dp.toPx()
                    val w = size.width
                    val path = Path().apply {
                        moveTo(halfStroke, size.height)
                        lineTo(halfStroke, r)
                        arcTo(
                            rect = Rect(halfStroke, halfStroke, r * 2 - halfStroke, r * 2 - halfStroke),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(w - r, halfStroke)
                        arcTo(
                            rect = Rect(w - r * 2 + halfStroke, halfStroke, w - halfStroke, r * 2 - halfStroke),
                            startAngleDegrees = 270f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(w - halfStroke, size.height)
                    }
                    drawPath(
                        path = path,
                        color = emphasisColor,
                        style = Stroke(width = strokeWidthPx)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
                    .animateContentSize()
            ) {
                CombinedTimeAdjustment()
                Spacer(modifier = Modifier.height(8.dp))
                DualTimePicker(baseTime = baseTime)
                Spacer(modifier = Modifier.height(8.dp))
                CombinedTimeAdjustment()
                Spacer(modifier = Modifier.height(16.dp))

                ActivityGridComponent(
                    activities = fakeActivities,
                    onActivityClick = { },
                    functionChipLabel = "活动",
                    functionChipIcon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "活动管理",
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    functionChipOnClick = { },
                    displayMode = activityConfig.displayMode.value,
                    layoutMode = activityConfig.layoutMode.value,
                    maxLinesPerColumn = activityConfig.columnLines.value,
                    maxLinesHorizontal = horizontalLines(activityConfig),
                    chipFixedWidth = 80.dp,
                    useActivityColorForText = activityConfig.useActivityColorForText.value,
                )
                Spacer(modifier = Modifier.height(10.dp))
                ActivityGridComponent(
                    activities = sampleTags,
                    onActivityClick = { },
                    functionChipLabel = "标签",
                    functionChipIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Label,
                            contentDescription = "标签管理",
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    functionChipOnClick = { },
                    displayMode = tagConfig.displayMode.value,
                    layoutMode = tagConfig.layoutMode.value,
                    maxLinesPerColumn = tagConfig.columnLines.value,
                    maxLinesHorizontal = horizontalLines(tagConfig),
                    chipFixedWidth = 50.dp,
                    useActivityColorForText = tagConfig.useActivityColorForText.value,
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
}

private fun horizontalLines(config: GridConfig): Int {
    val v = config.horizontalLines.value
    return if (v == 0) Int.MAX_VALUE else v
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
