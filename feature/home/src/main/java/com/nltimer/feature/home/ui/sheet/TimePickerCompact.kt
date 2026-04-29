package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 紧凑型时间选择器 Composable。
 * 点击显示 Material3 TimePicker 对话框，选择后更新显示。
 *
 * @param time 当前选择的时间
 * @param onTimeChange 时间变更回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerCompact(
    time: LocalTime?,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 控制时间选择器对话框的显示/隐藏
    var showPicker by remember { mutableStateOf(false) }

    // 弹出 Material3 TimePicker 对话框
    if (showPicker) {
        val now = LocalTime.now()
        val state = rememberTimePickerState(
            initialHour = time?.hour ?: now.hour,
            initialMinute = time?.minute ?: now.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Text(
                    text = "确定",
                    modifier = Modifier.clickable {
                        onTimeChange(LocalTime.of(state.hour, state.minute))
                        showPicker = false
                    },
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            text = { TimePicker(state = state) },
        )
    }

    // 显示当前时间文本，可点击打开选择器
    Text(
        text = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
