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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup

/**
 * 分组卡片组件
 *
 * 展示分组名称，支持展开/折叠列表，提供添加活动、重命名、删除等操作菜单。
 *
 * @param group 分组数据
 * @param activities 该分组下的活动列表
 * @param isExpanded 卡片是否展开
 * @param onToggleExpand 展开/折叠切换回调
 * @param onAddActivity 添加活动回调
 * @param onRename 重命名分组回调
 * @param onDelete 删除分组回调
 * @param onActivityClick 点击活动中回调
 * @param onActivityLongClick 长按活动回调
 * @param modifier 修饰符
 */
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
    // 控制三点菜单的展开状态
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 顶部行：分组名称 + 展开指示器 + 三点菜单按钮
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

                // 展开/折叠状态指示器
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 8.dp),
                )

                // 三点更多操作菜单
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

            // 展开时显示分组内活动列表（使用 FlowRow 自动换行）
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
                        activities.forEach { activity -> key(activity.id) {
                            ActivityChip(
                                activity = activity,
                                onClick = { onActivityClick(activity) },
                                onLongClick = { onActivityLongClick(activity) },
                            )
                        } }
                    }
                }
            }
        }
    }
}
