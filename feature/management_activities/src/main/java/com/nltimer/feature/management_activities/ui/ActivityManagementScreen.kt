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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.appOutlinedTextFieldColors
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.feature.management_activities.model.DialogState
import com.nltimer.feature.management_activities.viewmodel.ActivityManagementViewModel
import com.nltimer.feature.management_activities.ui.components.ActivityChip
import com.nltimer.feature.management_activities.ui.components.ActivityDetailSheet
import com.nltimer.feature.management_activities.ui.components.GroupCard
import com.nltimer.feature.management_activities.ui.components.dialogs.AddActivityFormSheet
import com.nltimer.feature.management_activities.ui.components.dialogs.AddGroupDialog
import com.nltimer.feature.management_activities.ui.components.dialogs.ConfirmDialog
import com.nltimer.feature.management_activities.ui.components.dialogs.EditActivityFormSheet
import com.nltimer.feature.management_activities.ui.components.dialogs.MoveToGroupDialog

/**
 * 活动管理主屏幕
 *
 * 展示未分类活动、自定义分组卡片，并管理各类弹窗的显示逻辑。
 *
 * @param viewModel 活动管理的 ViewModel
 * @param modifier 修饰符
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActivityManagementScreen(
    viewModel: ActivityManagementViewModel,
    modifier: Modifier = Modifier,
) {
    // 收集 UI 状态流，驱动界面刷新
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            // 右下角悬浮按钮：点击弹出添加活动弹窗
            FloatingActionButton(onClick = { viewModel.showAddActivityDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "添加活动")
            }
        },
        modifier = modifier,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // 根据状态显示：加载中、空状态或内容列表
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (
                uiState.uncategorizedActivities.isEmpty() &&
                uiState.groups.isEmpty()
            ) {
                // 没有任何活动时的空状态提示
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
                // 有内容时使用 LazyColumn 展示未分类区域和分组卡片列表
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        // 未分类活动区域标题
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
                            // 使用 FlowRow 让活动标签自动换行排列
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                uiState.uncategorizedActivities.forEach { activity ->
                                    ActivityChip(
                                        activity = activity,
                                        // 单击查看详情
                                        onClick = { viewModel.showActivityDetail(activity) },
                                        // 长按弹出移动分组弹窗
                                        onLongClick = {
                                            viewModel.showMoveToGroupDialog(activity)
                                        },
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 自定义分组区域标题
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

                    // 遍历渲染每个分组卡片
                    items(uiState.groups.size) { index ->
                        val groupWithActivities = uiState.groups[index]
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

    // 根据弹窗状态展示对应的弹窗组件
    when (val dialog = uiState.dialogState) {
        is DialogState.AddActivity -> {
            AddActivityFormSheet(
                allGroups = uiState.allGroups,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { name, emoji, color, groupId, note ->
                    viewModel.addActivity(name, emoji, color, groupId, note)
                },
            )
        }

        is DialogState.AddActivityToGroup -> {
            AddActivityFormSheet(
                allGroups = uiState.allGroups,
                initialGroupId = dialog.group.id,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { name, emoji, color, groupId, note ->
                    viewModel.addActivity(name, emoji, color, groupId, note)
                },
            )
        }

        is DialogState.EditActivity -> {
            EditActivityFormSheet(
                activity = dialog.activity,
                allGroups = uiState.allGroups,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { updatedActivity ->
                    viewModel.updateActivity(updatedActivity)
                },
            )
        }

        is DialogState.AddGroup -> {
            AddGroupDialog(
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { name -> viewModel.addGroup(name) },
            )
        }

        is DialogState.RenameGroup -> {
            RenameGroupDialog(
                currentName = dialog.group.name,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { newName ->
                    viewModel.renameGroup(dialog.group.id, newName)
                },
            )
        }

        is DialogState.DeleteGroup -> {
            ConfirmDialog(
                title = "删除分组",
                message = "确定要删除「${dialog.group.name}」吗？\n该分组下的所有活动将变为未分类状态。",
                confirmText = "删除",
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { viewModel.deleteGroup(dialog.group.id) },
            )
        }

        is DialogState.DeleteActivity -> {
            ConfirmDialog(
                title = "删除活动",
                message = "确定要删除「${dialog.activity.name}」吗？此操作不可撤销。",
                confirmText = "删除",
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { viewModel.deleteActivity(dialog.activity.id) },
            )
        }

        is DialogState.MoveToGroup -> {
            MoveToGroupDialog(
                activity = dialog.activity,
                allGroups = uiState.allGroups,
                onDismiss = { viewModel.dismissDialog() },
                onConfirm = { targetGroupId ->
                    viewModel.moveActivityToGroup(dialog.activity.id, targetGroupId)
                },
            )
        }

        is DialogState.ActivityDetail -> {
            val stats by viewModel.currentActivityStats.collectAsState()
            ActivityDetailSheet(
                activity = dialog.activity,
                stats = stats,
                allGroups = uiState.allGroups,
                onDismiss = { viewModel.dismissDialog() },
                onUpdate = { viewModel.updateActivity(it) },
                onDelete = { viewModel.showDeleteActivityDialog(dialog.activity) }
            )
        }

        null -> {}
    }
}

/**
 * 重命名分组的弹窗
 *
 * @param currentName 当前分组名称
 * @param onDismiss 关闭弹窗回调
 * @param onConfirm 确认重命名回调，参数为新名称
 */
@Composable
private fun RenameGroupDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    // 使用 currentName 作为输入框初始值，重命名时自动填充当前名称
    var name by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名分组") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("新名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = appOutlinedTextFieldColors(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                // 名称不能为空且必须与当前名称不同才能启用确认按钮
                enabled = name.isNotBlank() && name != currentName,
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
