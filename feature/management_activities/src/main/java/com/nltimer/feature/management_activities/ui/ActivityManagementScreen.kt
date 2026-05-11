package com.nltimer.feature.management_activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.designsystem.component.DragActionFab
import com.nltimer.core.designsystem.component.FabDragOptions
import com.nltimer.core.designsystem.component.rememberDragFabState
import com.nltimer.feature.management_activities.viewmodel.ActivityManagementViewModel
import com.nltimer.feature.management_activities.ui.components.ActivityChip
import com.nltimer.feature.management_activities.ui.components.GroupCard

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActivityManagementScreen(
    viewModel: ActivityManagementViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dragFabState = rememberDragFabState()

    Box(
        modifier = modifier
            .onGloballyPositioned { dragFabState.boxPositionInWindow = it.positionInWindow() }
    ) {
        Scaffold(
            floatingActionButton = {
                DragActionFab(
                    state = dragFabState,
                    icon = Icons.Default.Add,
                    onClick = { viewModel.showAddActivityDialog() },
                    onOptionSelected = { option ->
                        when (option) {
                            "添加活动" -> viewModel.showAddActivityDialog()
                            "添加分组" -> viewModel.showAddGroupDialog()
                        }
                    },
                )
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                } else if (
                    uiState.uncategorizedActivities.isEmpty() &&
                    uiState.groups.isEmpty()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Text("暂无活动", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "点击右下角按钮添加活动",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            Text(
                                text = "未分类",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            if (uiState.uncategorizedActivities.isEmpty()) {
                                Text(
                                    "暂无未分类活动",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    uiState.uncategorizedActivities.forEach { activity ->
                                        ActivityChip(
                                            activity = activity,
                                            onClick = { viewModel.showActivityDetail(activity) },
                                            onLongClick = {
                                                viewModel.showMoveToGroupDialog(activity)
                                            },
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "自定义分组",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }

                        items(items = uiState.groups, key = { it.group.id }) { groupWithActivities ->
                            GroupCard(
                                group = groupWithActivities.group,
                                activities = groupWithActivities.activities,
                                isExpanded = uiState.expandedGroupIds.contains(groupWithActivities.group.id),
                                onToggleExpand = {
                                    viewModel.toggleGroupExpand(groupWithActivities.group.id)
                                },
                                onAddActivity = {
                                    viewModel.showAddActivityToGroupDialog(groupWithActivities.group)
                                },
                                onRename = {
                                    viewModel.showRenameGroupDialog(groupWithActivities.group)
                                },
                                onDelete = {
                                    viewModel.showDeleteGroupDialog(groupWithActivities.group)
                                },
                                onActivityClick = { activity ->
                                    viewModel.showActivityDetail(activity)
                                },
                                onActivityLongClick = { activity ->
                                    viewModel.showMoveToGroupDialog(activity)
                                },
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        FabDragOptions(
            state = dragFabState,
            options = listOf("添加活动", "添加分组"),
        )

        ActivityManagementSheetRouter(
            uiState = uiState,
            viewModel = viewModel,
        )
    }
}
