package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.behaviorui.R

/**
 * 行为类型选择器 Composable。
 * 以 ● / ○ 单选形式展示待办、活跃、已完成三种类型。
 *
 * @param selected 当前选中的类型
 * @param onSelect 选择类型回调
 * @param modifier 修饰符
 */
@Composable
fun BehaviorNatureSelector(
    selected: BehaviorNature,
    onSelect: (BehaviorNature) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        BehaviorNature.entries.forEach { nature ->
            val isSelected = nature == selected
            val labelRes = when (nature) {
                BehaviorNature.PENDING -> R.string.behavior_nature_pending
                BehaviorNature.ACTIVE -> R.string.behavior_nature_active
                BehaviorNature.COMPLETED -> R.string.behavior_nature_completed
            }
            val symbol = if (isSelected) "●" else "○"
            
            Text(
                text = "${stringResource(labelRes)} $symbol",
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { onSelect(nature) },
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}
