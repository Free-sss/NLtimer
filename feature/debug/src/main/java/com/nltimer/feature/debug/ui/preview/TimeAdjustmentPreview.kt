package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 时间步进调节器调试预览入口
 * 展示当前时间并提供一个水平步进式按钮组来调节时间，
 * 点击 +-N 按钮对时间进行分钟级增减，点击"现在"按钮重置为系统当前时间
 */
@Preview(showBackground = true)
@Composable
fun TimeAdjustmentDebugPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = currentTime.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                ),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimeAdjustmentComponent(
                currentTime = currentTime,
                onTimeChanged = { currentTime = it },
            )
        }
    }
}

@Composable
internal fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 定义时间调整选项：-30, -5, -1, 1, 5
    val adjustments = listOf("-30", "-5", "-1", "+1", "+5", "+30")

    // 使用 Row 确保所有元素在一行内显示
    Row (
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 遍历生成时间步进按钮
        adjustments.forEach { text ->
            val amount = text.toInt()
            TimeButton(
                text = text,
            ) {
                onTimeChanged(currentTime.plusMinutes(amount.toLong()))
            }
        }
    }   
}



@Composable
private fun TimeButton(
    text: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 3.dp, vertical = 0.dp),
        modifier = Modifier.width(36.dp).height(28.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
    }
}
