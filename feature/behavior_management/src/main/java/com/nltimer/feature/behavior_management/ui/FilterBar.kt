package com.nltimer.feature.behavior_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.BehaviorNature

@Composable
fun FilterBar(
    activityGroups: List<String>,
    tagCategories: List<String>,
    selectedActivityGroup: String?,
    selectedTagCategory: String?,
    selectedStatus: BehaviorNature?,
    searchQuery: String,
    onActivityGroupChange: (String?) -> Unit,
    onTagCategoryChange: (String?) -> Unit,
    onStatusChange: (BehaviorNature?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activityExpanded by remember { mutableStateOf(false) }
    var tagExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilterDropdown(
                label = selectedActivityGroup ?: "活动分组",
                expanded = activityExpanded,
                onExpandChange = { activityExpanded = it },
            ) {
                DropdownMenuItem(
                    text = { Text("全部") },
                    onClick = {
                        onActivityGroupChange(null)
                        activityExpanded = false
                    },
                )
                activityGroups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group) },
                        onClick = {
                            onActivityGroupChange(group)
                            activityExpanded = false
                        },
                    )
                }
            }

            FilterDropdown(
                label = selectedTagCategory ?: "标签分类",
                expanded = tagExpanded,
                onExpandChange = { tagExpanded = it },
            ) {
                DropdownMenuItem(
                    text = { Text("全部") },
                    onClick = {
                        onTagCategoryChange(null)
                        tagExpanded = false
                    },
                )
                tagCategories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onTagCategoryChange(category)
                            tagExpanded = false
                        },
                    )
                }
            }

            FilterDropdown(
                label = selectedStatus?.let { statusLabel(it) } ?: "状态",
                expanded = statusExpanded,
                onExpandChange = { statusExpanded = it },
            ) {
                DropdownMenuItem(
                    text = { Text("全部") },
                    onClick = {
                        onStatusChange(null)
                        statusExpanded = false
                    },
                )
                BehaviorNature.entries.forEach { nature ->
                    DropdownMenuItem(
                        text = { Text(statusLabel(nature)) },
                        onClick = {
                            onStatusChange(nature)
                            statusExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    OutlinedButton(onClick = { onExpandChange(true) }) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandChange(false) },
    ) {
        content()
    }
}

private fun statusLabel(nature: BehaviorNature): String = when (nature) {
    BehaviorNature.COMPLETED -> "已完成"
    BehaviorNature.ACTIVE -> "进行中"
    BehaviorNature.PENDING -> "待定"
}
