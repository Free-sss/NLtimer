package com.nltimer.core.debugui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FieldDetailDialog(
    title: String,
    fields: List<FieldInfo>,
    rawJson: String,
    onDismiss: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("渲染", "原生")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, tabTitle ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tabTitle) },
                        )
                    }
                }

                when (selectedTab) {
                    0 -> RenderTab(fields = fields)
                    1 -> NativeTab(rawJson = rawJson)
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
private fun RenderTab(fields: List<FieldInfo>) {
    val displayedFields = fields.filter { it.isDisplayed }
    val missingFields = fields.filter { it.isMissing }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(displayedFields) { field ->
            FieldRow(field = field)
        }

        if (missingFields.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MissingFieldsSummary(missingFields = missingFields)
            }
        }
    }
}

@Composable
private fun FieldRow(field: FieldInfo) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = field.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = field.value?.toString() ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (field.isMissing) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
    )
}

@Composable
private fun MissingFieldsSummary(missingFields: List<FieldInfo>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
    ) {
        Text(
            text = "缺失字段",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = missingFields.joinToString(", ") { it.displayName },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun NativeTab(rawJson: String) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(rawJson))
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "复制 JSON",
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("复制全部")
        }

        Spacer(modifier = Modifier.height(12.dp))

        SelectionContainer {
            Text(
                text = rawJson,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(12.dp),
            )
        }
    }
}
