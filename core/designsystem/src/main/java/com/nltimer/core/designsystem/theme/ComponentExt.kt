package com.nltimer.core.designsystem.theme

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

@Composable
fun appAssistChipBorder(enabled: Boolean = true) = if (LocalTheme.current.showBorders) {
    AssistChipDefaults.assistChipBorder(enabled = enabled)
} else {
    null
}

@Composable
fun appInputChipBorder(enabled: Boolean = true, selected: Boolean = false) = if (LocalTheme.current.showBorders) {
    InputChipDefaults.inputChipBorder(enabled = enabled, selected = selected)
} else {
    null
}
