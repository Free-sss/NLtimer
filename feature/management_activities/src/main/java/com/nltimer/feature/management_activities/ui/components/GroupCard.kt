package com.nltimer.feature.management_activities.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupCard(
    group: ActivityGroup,
    activities: List<Activity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAddActivity: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onActivityClick: (Activity) -> Unit,
    onActivityLongClick: (Activity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "📂 ${group.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = if (isExpanded) "▼" else "▶",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("添加活动") },
                            onClick = {
                                showMenu = false
                                onAddActivity()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("重命名") },
                            onClick = {
                                showMenu = false
                                onRename()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                if (activities.isEmpty()) {
                    Text(
                        text = "暂无活动",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        activities.forEach { activity ->
                            ActivityChip(
                                activity = activity,
                                onClick = { onActivityClick(activity) },
                                onLongClick = { onActivityLongClick(activity) },
                            )
                        }
                    }
                }
            }
        }
    }
}
