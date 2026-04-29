package com.nltimer.core.designsystem.theme

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 根据全局边框开关返回 OutlinedTextField 的配色
 * 关闭边框时所有边框色设为透明
 */
@Composable
fun appOutlinedTextFieldColors() = if (LocalTheme.current.showBorders) {
    OutlinedTextFieldDefaults.colors()
} else {
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
    )
}

/**
 * 根据全局边框开关返回 AssistChip 边框
 * @param enabled 是否启用边框样式
 */
@Composable
fun appAssistChipBorder(enabled: Boolean = true) = if (LocalTheme.current.showBorders) {
    AssistChipDefaults.assistChipBorder(enabled = enabled)
} else {
    null
}

/**
 * 根据全局边框开关返回 InputChip 边框
 * @param enabled 是否启用边框样式
 * @param selected 是否选中状态
 */
@Composable
fun appInputChipBorder(enabled: Boolean = true, selected: Boolean = false) = if (LocalTheme.current.showBorders) {
    InputChipDefaults.inputChipBorder(enabled = enabled, selected = selected)
} else {
    null
}
