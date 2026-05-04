package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.TimeLabelConfig
import com.nltimer.core.designsystem.theme.TimeLabelFormat
import com.nltimer.core.designsystem.theme.TimeLabelStyle
import com.nltimer.core.designsystem.theme.toDisplayString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLabelSettingsDialog(
    config: TimeLabelConfig,
    onConfigChange: (TimeLabelConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(config.visible) }
    var style by remember { mutableStateOf(config.style) }
    var format by remember { mutableStateOf(config.format) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "时间标题外观",
                    style = MaterialTheme.typography.headlineSmall,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "显示时间标签",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = visible,
                        onCheckedChange = { visible = it },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "标签样式",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        TimeLabelStyle.entries.forEachIndexed { index, entry ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = TimeLabelStyle.entries.size,
                                ),
                                onClick = { style = entry },
                                selected = style == entry,
                                enabled = visible,
                            ) {
                                Text(
                                    text = entry.toDisplayString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "时间格式",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        TimeLabelFormat.entries.forEachIndexed { index, entry ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = TimeLabelFormat.entries.size,
                                ),
                                onClick = { format = entry },
                                selected = format == entry,
                                enabled = visible,
                            ) {
                                Text(
                                    text = entry.toDisplayString(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = {
                            onConfigChange(TimeLabelConfig(visible, style, format))
                            onDismiss()
                        },
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
