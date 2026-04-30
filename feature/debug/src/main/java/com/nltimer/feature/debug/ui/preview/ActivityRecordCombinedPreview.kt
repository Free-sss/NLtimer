package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Column
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

/**
 * 活动记录组合弹窗调试预览入口
 * 将双列时间选择器、时间步进调节器、活动标签网格、活动备注输入
 * 四个独立调试组件组合到一个 AlertDialog 中展示。
 * 各组件通过 fake 模拟数据独立运行，点击按钮可打开/关闭弹窗
 */
@Preview(showBackground = true)
@Composable
fun ActivityRecordCombinedPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        ActivityRecordCombinedDialog { }
    }
}

/**
 * 活动记录组合弹窗
 * 使用半屏可拖动的 ModalBottomSheet 垂直排列四个组件：
 * 双列时间选择器、时间步进调节器、活动标签网格、活动备注输入
 *
 * @param onDismiss 关闭弹窗的回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityRecordCombinedDialog(
    onDismiss: () -> Unit,
) {
    // 弹窗打开时的时间锚点，传递给 DualTimePicker
    val baseTime = remember { LocalDateTime.now() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

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
                modifier = Modifier.padding(top = 12.dp),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // 1. 双列时间选择器
            SectionLabel("时间范围")
            DualTimePicker(baseTime = baseTime)

            // 2. 时间步进调节器
            SectionLabel("时间微调")
            CombinedTimeAdjustment()

            // 3. 活动标签网格
            SectionLabel("选择活动")
            ActivityGridComponent(
                activities = fakeActivities,
                onActivityClick = { },
                onManageClick = { },
                onAddClick = { },
            )

            // 4. 活动备注输入
            SectionLabel("活动备注")
            ActivityNoteComponent(
                onLabelClick = { },
                onHistoryClick = { },
                onContinueAddClick = { },
                onAddClick = { },
            )

            // 底部操作按钮
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                ) {
                    Text("取消", color = Color.Gray, fontSize = 14.sp)
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006064)),
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
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = Color(0xFF0288D1),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}
