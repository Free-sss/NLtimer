package com.nltimer.feature.debug.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry

/**
 * 调试组件展示页面
 * 全屏预览区 + 右下角 FAB + 底部弹窗选择器的布局。
 * 从 [DebugComponentRegistry] 读取所有已注册的调试组件，
 * 用户通过弹窗选择分组和组件后，在预览区全屏渲染该组件
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DebugPage() {
    // 从 Registry 读取所有已注册的调试组件
    val allComponents = remember { DebugComponentRegistry.components }
    // 提取不重复的分组名，前面加"全部"
    val groups = remember(allComponents) {
        listOf("全部") + allComponents.map { it.group }.distinct()
    }

    // 当前选中的分组和组件
    var selectedGroup by remember { mutableStateOf("全部") }
    var selectedComponentId by remember { mutableStateOf<String?>(null) }
    // 控制底部弹窗的显隐
    var showPicker by remember { mutableStateOf(false) }

    // 按选中的分组过滤组件列表
    val filteredComponents = remember(selectedGroup, allComponents) {
        if (selectedGroup == "全部") allComponents
        else allComponents.filter { it.group == selectedGroup }
    }

    // 根据选中的 id 查找当前组件
    val selectedComponent = remember(selectedComponentId, allComponents) {
        allComponents.find { it.id == selectedComponentId }
    }

    // 弹窗状态：skipPartiallyExpanded 使其始终全展开
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 空状态：Registry 中没有任何已注册组件时显示提示
    if (allComponents.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDCE6",
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "暂无已注册的调试组件",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "在 feature 模块的 src/debug/ 下注册组件后即可显示",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        return
    }

    // 使用 Scaffold 提供 FAB 悬浮按钮
    Scaffold(
        floatingActionButton = {
            // 右下角 FAB：点击弹出组件选择弹窗
            FloatingActionButton(
                onClick = { showPicker = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "选择组件",
                )
            }
        },
    ) { padding ->
        // 全屏预览区域
        PreviewArea(
            component = selectedComponent,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }

    // 组件选择弹窗
    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = sheetState,
        ) {
            ComponentPickerSheet(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelected = { selectedGroup = it },
                components = filteredComponents,
                selectedComponentId = selectedComponentId,
                onComponentSelected = { component ->
                    selectedComponentId = component.id
                    // 选择组件后自动关闭弹窗
                    showPicker = false
                },
            )
        }
    }
}

/**
 * 组件选择弹窗内容
 * 顶部 FlowRow 显示分组标签，下方 LazyColumn 列出该分组下的组件
 *
 * @param groups 所有分组名称列表
 * @param selectedGroup 当前选中的分组
 * @param onGroupSelected 分组选中回调
 * @param components 过滤后的组件列表
 * @param selectedComponentId 当前选中的组件 id
 * @param onComponentSelected 组件选中回调
 */
@Composable
private fun ComponentPickerSheet(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    components: List<DebugComponent>,
    selectedComponentId: String?,
    onComponentSelected: (DebugComponent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "选择调试组件",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        // 分组标签：使用 FlowRow 自动换行
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            groups.forEach { group ->
                val isSelected = group == selectedGroup
                // 使用胶囊形 Surface 作为分组标签
                Surface(
                    onClick = { onGroupSelected(group) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        text = if (group == "全部") "\uD83C\uDFF7\uFE0F 全部" else group,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 组件列表
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(components) { component ->
                val isSelected = component.id == selectedComponentId
                Surface(
                    onClick = { onComponentSelected(component) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 组件名称
                            Text(
                                text = component.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                            )
                            if (component.implemented) {
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                ) {
                                    Text(
                                        text = "已实装",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                    )
                                }
                            }
                        }
                        // 组件描述（如果有）
                        if (component.description.isNotEmpty()) {
                            Text(
                                text = component.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 预览区域
 * 未选择组件时显示引导提示；选中组件时显示组件名称头栏并渲染组件内容
 *
 * @param component 当前选中的调试组件，为 null 时显示空状态
 * @param modifier 可选的修饰符
 */
@Composable
private fun PreviewArea(
    component: DebugComponent?,
    modifier: Modifier = Modifier,
) {
    if (component == null) {
        // 未选择组件：提示用户操作
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDC46",
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "点击右下角按钮选择组件",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
    } else {
        // 已选择组件：标题栏 + 内容
        Column(modifier = modifier) {
            // 组件信息标题栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "▼ ${component.name}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = component.group,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            // 组件内容渲染区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                component.content()
            }
        }
    }
}
