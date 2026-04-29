package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.feature.debug.model.FormRow
import com.nltimer.feature.debug.ui.GenericFormSheet

@Composable
fun AddTagPreview() {
    val mockGroups = remember { MockData.groups }
    var showSheet by remember { mutableStateOf(true) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "点击下方按钮打开新增标签弹窗",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { showSheet = true }) {
                Text("打开新增标签弹窗")
            }
        }
    }

    if (showSheet) {
        GenericFormSheet(
            spec = ActivityFormSpecs.createTag.copy(
                sections = ActivityFormSpecs.createTag.sections.map { section ->
                    section.copy(
                        rows = section.rows.map { row ->
                            if (row is FormRow.LabelAction && row.key == "category") {
                                row.copy(
                                    actionText = mockGroups.find { it.id == selectedGroupId }?.name ?: "未分类",
                                    onClick = { showGroupPicker = true },
                                )
                            } else row
                        },
                    )
                },
            ),
            initialData = null,
            onDismiss = { showSheet = false },
            onSubmit = { showSheet = false },
            overlay = if (showGroupPicker) {
                { GroupPickerPopup(groups = mockGroups, selectedId = selectedGroupId, onSelected = { selectedGroupId = it }, onDismiss = { showGroupPicker = false }) }
            } else null,
        )
    }
}
