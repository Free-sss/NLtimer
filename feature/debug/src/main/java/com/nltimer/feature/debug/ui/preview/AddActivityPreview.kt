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
import com.nltimer.core.designsystem.form.ActivityFormSpecs
import com.nltimer.core.designsystem.form.FormRow
import com.nltimer.core.designsystem.form.GenericFormSheet

@Composable
fun AddActivityPreview() {
    val mockTags = remember { MockData.tags }
    val mockGroups = remember { MockData.groups }
    var showSheet by remember { mutableStateOf(true) }
    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showGroupPicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "点击下方按钮打开新增活动弹窗",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { showSheet = true }) {
                Text("打开新增活动弹窗")
            }
        }
    }

    if (showSheet) {
        GenericFormSheet(
            spec = ActivityFormSpecs.createActivity.copy(
                sections = ActivityFormSpecs.createActivity.sections.map { section ->
                    section.copy(
                        rows = section.rows.map { row ->
                            if (row is FormRow.LabelAction) {
                                when (row.key) {
                                    "tags" -> row.copy(onClick = { showTagPicker = true })
                                    "category" -> row.copy(
                                        actionText = mockGroups.find { it.id == selectedGroupId }?.name ?: "未分类",
                                        onClick = { showGroupPicker = true },
                                    )
                                    else -> row
                                }
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

    if (showTagPicker) {
        TagPickerSheet(
            tags = mockTags,
            selectedIds = selectedTagIds,
            onIdsChanged = { selectedTagIds = it },
            onDismiss = { showTagPicker = false },
        )
    }
}
