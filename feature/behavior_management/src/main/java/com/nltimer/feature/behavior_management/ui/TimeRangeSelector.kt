package com.nltimer.feature.behavior_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appAssistChipBorder
import com.nltimer.feature.behavior_management.model.TimeRangePreset
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSelector(
    currentPreset: TimeRangePreset,
    currentDate: LocalDate,
    onPresetChange: (TimeRangePreset) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onNavigate: (direction: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var presetExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box {
            AssistChip(
                onClick = { presetExpanded = true },
                label = {
                    Text(text = currentPreset.label, style = MaterialTheme.typography.labelMedium)
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
                border = appAssistChipBorder(),
            )
            DropdownMenu(
                expanded = presetExpanded,
                onDismissRequest = { presetExpanded = false },
            ) {
                TimeRangePreset.entries.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.label) },
                        onClick = {
                            onPresetChange(preset)
                            presetExpanded = false
                        },
                    )
                }
            }
        }

        IconButton(onClick = { onNavigate(-1) }, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "前一天",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AssistChip(
            onClick = { showDatePicker = true },
            label = {
                Text(
                    text = formatDate(currentDate),
                    style = MaterialTheme.typography.labelMedium,
                )
            },
            border = appAssistChipBorder(),
        )

        IconButton(onClick = { onNavigate(1) }, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "后一天",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDatePicker) {
        val initialMillis = currentDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateChange(selected)
                    }
                    showDatePicker = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.year}/${date.monthValue}/${date.dayOfMonth}"
}

