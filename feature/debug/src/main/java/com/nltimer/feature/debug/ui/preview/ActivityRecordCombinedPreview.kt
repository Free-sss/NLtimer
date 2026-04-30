package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime

/**
 * 活动记录组合弹窗调试预览入口
 * 将双列时间选择器、时间步进调节器、活动标签网格、活动备注输入
 * 四个独立调试组件组合到一个 AlertDialog 中展示。
 * 各组件通过 fake 模拟数据独立运行，点击按钮可打开/关闭弹窗
 */
@Composable
fun ActivityRecordCombinedPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        // 控制弹窗显示状态
        var showDialog by remember { mutableStateOf(false) }

        // 居中放置打开按钮
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "组合弹窗包含：双列时间选择器 + 步进调节 + 标签网格 + 备注输入",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showDialog = true }) {
                Text("打开活动记录组合弹窗")
            }
        }

        if (showDialog) {
            ActivityRecordCombinedDialog(
                onDismiss = { showDialog = false },
            )
        }
    }
}

/**
 * 活动记录组合弹窗
 * 垂直排列四个组件：双列时间选择器、时间步进调节器、活动标签网格、活动备注输入
 *
 * @param onDismiss 关闭弹窗的回调
 */
@Composable
private fun ActivityRecordCombinedDialog(
    onDismiss: () -> Unit,
) {
    // fake 模拟数据：活动标签网格
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "活动记录",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // 1. 双列时间选择器
                SectionLabel("时间范围")
                DualTimePicker()

                // 2. 时间步进调节器
                SectionDivider()
                SectionLabel("时间微调")
                CombinedTimeAdjustment()

                // 3. 活动标签网格
                SectionDivider()
                SectionLabel("选择活动")
                ActivityGridComponent(
                    activities = fakeActivities,
                    onActivityClick = { },
                    onManageClick = { },
                    onAddClick = { },
                )

                // 4. 活动备注输入
                SectionDivider()
                SectionLabel("活动备注")
                ActivityNoteComponent(
                    onLabelClick = { },
                    onHistoryClick = { },
                    onContinueAddClick = { },
                    onAddClick = { },
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

/**
 * 组合弹窗内专用的时间步进调节器包装
 * 内聚其自身的 [LocalDateTime] 状态，对外无依赖
 */
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

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(vertical = 4.dp),
    )
}
