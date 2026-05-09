package com.nltimer.core.designsystem.form

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GenericFormDialog(
    spec: FormSpec,
    initialData: Map<String, String>?,
    onDismiss: () -> Unit,
    onSubmit: (Map<String, String>) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val formState = remember {
        val defaults = spec.defaultValues().toMutableMap()
        if (initialData != null) {
            defaults.putAll(initialData)
        }
        mutableStateMapOf<String, String>().also { it.putAll(defaults) }
    }

    val confirmEnabled = formState["name"]?.isNotBlank() == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(spec.title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                spec.sections.forEachIndexed { index, section ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            section.rows.forEach { row ->
                                formRowRenderer(row = row, formState = formState)
                            }
                        }
                    }
                }

                trailing?.invoke()
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(formState.toMap()) },
                enabled = confirmEnabled,
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}


