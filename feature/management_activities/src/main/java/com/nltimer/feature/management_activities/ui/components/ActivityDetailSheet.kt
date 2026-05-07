package com.nltimer.feature.management_activities.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.ActivityStats
import com.nltimer.core.data.util.formatDurationMinutes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 活动详情底部弹出表单
 *
 * 展示活动统计信息，支持切换编辑模式修改名称、图标和所属分组。
 *
 * @param activity 当前查看的活动
 * @param stats 活动统计数据
 * @param allGroups 全部分组列表（用于分组选择下拉）
 * @param onDismiss 关闭底部表单回调
 * @param onUpdate 更新活动回调
 * @param onDelete 删除活动回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailSheet(
    activity: Activity,
    stats: ActivityStats,
    allGroups: List<ActivityGroup>,
    onDismiss: () -> Unit,
    onUpdate: (Activity) -> Unit,
    onDelete: () -> Unit,
) {
    // 底部弹窗默认完全展开，不可半展开
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // 控制当前是否为编辑模式
    var isEditing by remember { mutableStateOf(false) }

    // 编辑模式下各字段的临时状态，初始值从 activity 同步
    var name by remember(activity.name) { mutableStateOf(activity.name) }
    var iconKey by remember(activity.iconKey) { mutableStateOf(activity.iconKey ?: "") }
    var selectedGroupId by remember(activity.groupId) { mutableStateOf(activity.groupId) }
    // 分组下拉菜单展开状态
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 顶部：活动 Emoji + 名称 + 编辑/删除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activity.iconKey ?: "📌",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = if (isEditing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                // 编辑模式：显示名称、Emoji、分组下拉和保存按钮
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("名称") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = iconKey,
                        onValueChange = { if (it.length <= 2) iconKey = it },
                        label = { Text("图标") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 分组选择下拉框
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                    ) {
                        val groupName = if (selectedGroupId == null) "未分类" else allGroups.find { it.id == selectedGroupId }?.name ?: "未知分类"
                        OutlinedTextField(
                            value = groupName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("所属分组") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            // "未分类"选项，对应 groupId = null
                            DropdownMenuItem(
                                text = { Text("未分类") },
                                onClick = {
                                    selectedGroupId = null
                                    expanded = false
                                },
                            )
                            allGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.id
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onUpdate(activity.copy(name = name, iconKey = iconKey.ifBlank { null }, groupId = selectedGroupId))
                            isEditing = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && (name != activity.name || iconKey != (activity.iconKey ?: "") || selectedGroupId != activity.groupId)
                    ) {
                        Text("保存修改")
                    }
                }
            } else {
                // 非编辑模式：显示活动统计信息
                Text(
                    text = "简单统计",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatRow("累计使用次数", "${stats.usageCount} 次")
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        StatRow("累计总时长", formatDurationMinutes(stats.totalDurationMinutes))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        StatRow("最近一次使用", formatTimestamp(stats.lastUsedTimestamp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 统计信息的一行展示
 *
 * @param label 统计项名称
 * @param value 统计值
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}


/**
 * 将时间戳格式化为可读日期字符串
 *
 * @param timestamp 毫秒时间戳，为 null 或 0 表示从未使用
 * @return 如 "2024-01-15 14:30" 的格式化字符串
 */
private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "从未使用"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
