import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime

@Composable
fun TimeAdjustmentComponent(
    currentTime: LocalDateTime,
    onTimeChanged: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    // 定义需要增减的数值
    val adjustments = listOf(-30, -5, -1, 1, 5, 30)

    // 使用 Row 和 horizontalScroll 确保在小屏幕上能够横向滑动
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 遍历生成数字按钮
        adjustments.forEach { amount ->
            val text = if (amount > 0) "+$amount" else "$amount"
            TimeButton(
                text = text,
                onClick = { 
                    // 核心逻辑：对传入的时间进行增减（这里以天为单位）
                    onTimeChanged(currentTime.plusMinutes(amount.toLong())) 
                }
            )
        }

        // 生成“现在”按钮
        TimeButton(
            text = "现在",
            onClick = { 
                // 核心逻辑：重置为当前最新时间
                onTimeChanged(LocalDateTime.now()) 
            }
        )
    }
}

@Composable
private fun TimeButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        // 还原截图中的圆角矩形，而不是 MD3 默认的全圆角 (StadiumShape)
        shape = RoundedCornerShape(8.dp),
        // 调整内边距使其更紧凑，贴合截图比例
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            // 使用 MD3 的表面颜色映射
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        // 使用 MD3 的边框变体颜色，让边框显得柔和
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = text, 
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Normal
            )
        )
    }
}