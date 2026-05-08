package com.nltimer.core.behaviorui.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

/**
 * 备注输入框 Composable。
 * 多行文本输入，用于补充行为细节描述。
 *
 * @param note 当前备注文本
 * @param onNoteChange 备注内容变更回调
 * @param modifier 修饰符
 */
@Composable
fun NoteInput(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 最多 3 行的备注输入框
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        placeholder = {
            Text(
                text = "补充描述这次行为的细节...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = appOutlinedTextFieldColors(),
        textStyle = MaterialTheme.typography.bodySmall,
        maxLines = 3,
        keyboardOptions = KeyboardOptions.Default,
    )
}
