package com.nltimer.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initialValue: String = "",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    validate: (String) -> Boolean = { it.isNotBlank() },
    confirmText: String = "确定",
    dismissText: String = "取消",
    enableCondition: ((String, String) -> Boolean)? = null,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }
    val trimmed = value.trim()
    val isValid = validate(trimmed)
    val extraCheck = enableCondition?.invoke(trimmed, initialValue) ?: true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = appOutlinedTextFieldColors(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(trimmed) },
                enabled = isValid && extraCheck,
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier,
    )
}
