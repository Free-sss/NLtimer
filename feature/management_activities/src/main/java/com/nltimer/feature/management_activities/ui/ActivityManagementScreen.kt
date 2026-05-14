package com.nltimer.feature.management_activities.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.behaviorui.sheet.CategoryGroupCard
import com.nltimer.core.behaviorui.sheet.CategorizableItem
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.designsystem.component.BottomBarDragFab
import com.nltimer.core.designsystem.component.EmptyStateView
import com.nltimer.core.designsystem.component.LoadingScreen
import com.nltimer.core.designsystem.component.rememberDragFabState
import com.nltimer.feature.management_activities.viewmodel.ActivityManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                if (uiState.isLoading) {
                    LoadingScreen()
                } else if (
                    uiState.uncategorizedActivities.isEmpty() &&
                    uiState.groups.isEmpty()
                ) {
                    EmptyStateView(
                        message = "暂无活动",
                        subtitle = "点击右下角按钮添加活动",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            val items = uiState.uncategorizedActivities.map { activity ->
                                ManagementActivityItem(activity)
                            }
                            CategoryGroupCard(
                                index = 0,
                                groupName = "未分类",
                                items = items,
                                collapsed = false,
                                showDragHandle = false,
                                emptyText = "暂无未分类活动",
                                onItemSelected = { id ->
                                    uiState.uncategorizedActivities
                                        .firstOrNull { it.id == id }
                                        ?.let(viewModel::showActivityDetail)
                                },
                                onItemLongClick = { id ->
                                    uiState.uncategorizedActivities
                                        .firstOrNull { it.id == id }
                                        ?.let(viewModel::showMoveToGroupDialog)
                                },
                                onAddItem = { viewModel.showAddActivityDialog() },
                            )
                        }

                        itemsIndexed(
                            items = uiState.groups,
                            key = { _, groupWithActivities -> groupWithActivities.group.id },
                        ) { index, groupWithActivities ->
                            val items = groupWithActivities.activities.map { activity ->
                                ManagementActivityItem(activity)
                            }
                            CategoryGroupCard(
                                index = index + 1,
                                groupName = groupWithActivities.group.name,
                                items = items,
                                collapsed = !uiState.expandedGroupIds.contains(groupWithActivities.group.id),
                                showDragHandle = false,
                                emptyText = "暂无活动",
                                onToggleCollapsed = {
                                    viewModel.toggleGroupExpand(groupWithActivities.group.id)
                                },
                                headerActions = {
                                    ActivityGroupActions(
                                        group = groupWithActivities.group,
                                        onAddActivity = viewModel::showAddActivityToGroupDialog,
                                        onRename = viewModel::showRenameGroupDialog,
                                        onDelete = viewModel::showDeleteGroupDialog,
                                    )
                                },
                                onItemSelected = { id ->
                                    groupWithActivities.activities
                                        .firstOrNull { it.id == id }
                                        ?.let(viewModel::showActivityDetail)
                                },
                                onItemLongClick = { id ->
                                    groupWithActivities.activities
                                        .firstOrNull { it.id == id }
                                        ?.let(viewModel::showMoveToGroupDialog)
                                },
                                onAddItem = {
                                    viewModel.showAddActivityToGroupDialog(groupWithActivities.group)
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

        BottomBarDragFab(
            state = dragFabState,
            icon = Icons.Default.Add,
            dragOptions = listOf("添加活动", "添加分组"),
            onClick = { viewModel.showAddActivityDialog() },
            onOptionSelected = { option ->
                when (option) {
                    "添加活动" -> viewModel.showAddActivityDialog()
                    "添加分组" -> viewModel.showAddGroupDialog()
                }
            },
        )

        ActivityManagementSheetRouter(
            uiState = uiState,
            viewModel = viewModel,
        )
    }
}

private data class ManagementActivityItem(
    val activity: Activity,
) : CategorizableItem {
    override val itemId: Long = activity.id
    override val itemName: String = activity.name
    override val category: String? = null
    override val usageCount: Int = activity.usageCount
    override val lastUsedTimestamp: Long? = null
    override val iconKey: String? = activity.iconKey
}

@Composable
private fun ActivityGroupActions(
    group: ActivityGroup,
    onAddActivity: (ActivityGroup) -> Unit,
    onRename: (ActivityGroup) -> Unit,
    onDelete: (ActivityGroup) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("添加活动") },
                onClick = {
                    expanded = false
                    onAddActivity(group)
                },
            )
            DropdownMenuItem(
                text = { Text("重命名") },
                onClick = {
                    expanded = false
                    onRename(group)
                },
            )
            DropdownMenuItem(
                text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDelete(group)
                },
            )
        }
    }
}
