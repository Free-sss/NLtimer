package com.nltimer.feature.debug.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 时间步进调节器调试预览入口
 * 展示当前时间并提供一个水平步进式按钮组来调节时间，
 * 点击 +-N 按钮对时间进行分钟级增减，点击"现在"按钮重置为系统当前时间
 */
@Composable
fun TimeAdjustmentDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        // 使用当前系统时间作为初始值
        var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 顶部时间显示区域，格式为 yyyy-MM-dd HH:mm:ss
            Text(
                text = currentTime.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 时间调整按钮组
            TimeAdjustmentComponent(
                currentTime = currentTime,
                onTimeChanged = { currentTime = it },
            )
        }
    }
}

/**
 * 时间步进调节组件
 * 提供一系列 OutlinedButton 来快速调整时间，支持分钟级的增减操作。
 * 使用 horizontalScroll 确保小屏幕上按钮不会被截断
 *
 * @param currentTime 当前显示的时间
 * @param onTimeChanged 时间变化时的回调
 * @param modifier 可选的修饰符
 */
@Composable
private fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 定义时间调整选项：-30, -5, -1, 1, 5, 30 分钟
    val adjustments = listOf(-30, -5, -1, 1, 5, 30)

    // 使用 Row 和 horizontalScroll 确保在小屏幕上能够横向滑动
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 遍历生成时间步进按钮
        adjustments.forEach { amount ->
            // 正值前加 + 号，负值自带 - 号
            val text = if (amount > 0) "+$amount" else "$amount"
            TimeButton(
                text = text,
                onClick = {
                    // 核心逻辑：对传入的时间进行分钟级增减
                    onTimeChanged(currentTime.plusMinutes(amount.toLong()))
                },
            )
        }

        // "现在"按钮：重置为系统当前时间
        TimeButton(
            text = "现在",
            onClick = {
                // 核心逻辑：重置为当前最新系统时间
                onTimeChanged(LocalDateTime.now())
            },
        )
    }
}

/**
 * 时间步进按钮组件
 * 带圆角矩形边框的 OutlinedButton，用于显示时间步进值或操作文本
 *
 * @param text 按钮上显示的文本，如 "+5"、"-1"、"现在"
 * @param onClick 按钮点击时的回调
 */
@Composable
private fun TimeButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        // 圆角矩形，而不是 MD3 默认的全圆角 StadiumShape，贴合截图比例
        shape = RoundedCornerShape(8.dp),
        // 调整内边距使其更紧凑
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        // 使用 MD3 的表面文字颜色
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        // 使用 MD3 的边框变体颜色，让边框显得柔和
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}
