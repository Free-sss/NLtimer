package com.nltimer.core.designsystem.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.component.ColorPickerDialog
import com.nltimer.core.designsystem.icon.IconPickerSheet
import com.nltimer.core.designsystem.icon.IconRenderer
import kotlin.text.toBooleanStrictOrNull

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
                                dialogFormRowRenderer(row = row, formState = formState)
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

@Composable
private fun dialogFormRowRenderer(
    row: FormRow,
    formState: MutableMap<String, String>,
) {
    when (row) {
        is FormRow.TextInput -> dialogTextInputRenderer(
            row = row,
            value = formState[row.key] ?: row.initialValue,
            onValueChange = { formState[row.key] = it },
        )
        is FormRow.IconColor -> dialogIconColorRenderer(
            row = row,
            emoji = formState[row.iconKey] ?: row.initialEmoji,
            colorValue = formState[row.colorKey] ?: "",
            onEmojiChange = { formState[row.iconKey] = it },
            onColorChange = { formState[row.colorKey] = it },
        )
        is FormRow.LabelAction -> dialogLabelActionRenderer(row = row)
        is FormRow.Switch -> dialogSwitchRenderer(
            row = row,
            checked = formState[row.key]?.toBooleanStrictOrNull() ?: row.initialChecked,
            onCheckedChange = { formState[row.key] = it.toString() },
        )
        is FormRow.NumberInput -> dialogNumberInputRenderer(
            row = row,
            value = formState[row.key]?.toIntOrNull() ?: row.initialValue,
            onValueChange = { formState[row.key] = it.toString() },
        )
    }
}

@Composable
private fun dialogTextInputRenderer(
    row: FormRow.TextInput,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            text = row.placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun dialogIconColorRenderer(
    row: FormRow.IconColor,
    emoji: String,
    colorValue: String,
    onEmojiChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
) {
    val fallbackColor = MaterialTheme.colorScheme.primary
    val currentColor = remember(colorValue) {
        try {
            if (colorValue.isNotBlank()) Color(colorValue.toULong(16).toLong())
            else fallbackColor
        } catch (_: Exception) {
            fallbackColor
        }
    }

    var showIconPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "图标",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { showIconPicker = true },
                contentAlignment = Alignment.Center,
            ) {
                IconRenderer(iconKey = emoji.ifBlank { null }, iconSize = 20.dp)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                "颜色",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .clickable { showColorPicker = true },
            )
        }
    }

    if (showIconPicker) {
        IconPickerSheet(
            currentIconKey = emoji.ifBlank { null },
            onIconSelected = { newIconKey ->
                onEmojiChange(newIconKey ?: "")
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false },
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = currentColor,
            onSelect = { color ->
                val hex = color.toArgb().toULong().toString(16)
                onColorChange(hex)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false },
        )
    }
}

@Composable
private fun dialogLabelActionRenderer(row: FormRow.LabelAction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (row.showHelp) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .then(if (row.onClick != null) Modifier.clickable { row.onClick() } else Modifier)
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            Text(
                text = row.actionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun dialogSwitchRenderer(
    row: FormRow.Switch,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun dialogNumberInputRenderer(
    row: FormRow.NumberInput,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        BasicTextField(
            value = value.toString(),
            onValueChange = { text ->
                val num = text.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                onValueChange(num.coerceIn(row.range))
            },
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
            singleLine = true,
        )
    }
}
