package com.nltimer.feature.debug.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry

/**
 * Debug 页面主入口。
 * 左侧分组侧边栏、中间组件列表、右侧预览区三栏布局。
 * 无注册组件时展示空状态引导提示。
 */
@Composable
fun DebugPage() {
    // 获取所有已注册的调试组件
    val allComponents = remember { DebugComponentRegistry.components }
    // 提取全部分组名列表，首项固定为"全部"
    val groups = remember(allComponents) {
        listOf("全部") + allComponents.map { it.group }.distinct()
    }

    // 当前选中的分组和组件
    var selectedGroup by remember { mutableStateOf("全部") }
    var selectedComponentId by remember { mutableStateOf<String?>(null) }

    // 根据选中分组过滤组件列表
    val filteredComponents = remember(selectedGroup, allComponents) {
        if (selectedGroup == "全部") allComponents
        else allComponents.filter { it.group == selectedGroup }
    }

    // 根据 id 找到当前选中的组件对象
    val selectedComponent = remember(selectedComponentId, allComponents) {
        allComponents.find { it.id == selectedComponentId }
    }

    // 空状态：没有任何已注册组件时显示引导提示
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

    // 三栏布局：分组 | 组件列表 | 预览区
    Row(modifier = Modifier.fillMaxSize()) {
        GroupSidebar(
            groups = groups,
            selectedGroup = selectedGroup,
            onGroupSelected = { selectedGroup = it },
            modifier = Modifier.width(120.dp).fillMaxHeight(),
        )
        ComponentList(
            components = filteredComponents,
            selectedComponentId = selectedComponentId,
            onComponentSelected = { selectedComponentId = it.id },
            modifier = Modifier.width(180.dp).fillMaxHeight(),
        )
        PreviewArea(
            component = selectedComponent,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
    }
}

/**
 * 分组侧边栏。
 * 展示所有组件分组名称，点击切换选中分组以过滤组件列表。
 *
 * @param groups 全部分组名称列表
 * @param selectedGroup 当前选中的分组
 * @param onGroupSelected 分组选中回调
 * @param modifier 修饰符
 */
@Composable
private fun GroupSidebar(
    groups: List<String>,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 2.dp,
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            // 分组列表标题
            item {
                Text(
                    text = "分组",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            // 遍历渲染每个分组项
            items(groups) { group ->
                val isSelected = group == selectedGroup
                Surface(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                        .clickable { onGroupSelected(group) },
                    shape = RoundedCornerShape(8.dp),
                    // 选中态使用主色容器背景，非选中态保持表面色
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Text(
                        text = if (group == "全部") "\uD83C\uDFF7\uFE0F 全部" else group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * 组件列表。
 * 展示当前分组过滤后的所有调试组件，点击选中后可在预览区查看。
 *
 * @param components 当前分组下的组件列表
 * @param selectedComponentId 当前选中组件的 id
 * @param onComponentSelected 组件选中回调
 * @param modifier 修饰符
 */
@Composable
private fun ComponentList(
    components: List<DebugComponent>,
    selectedComponentId: String?,
    onComponentSelected: (DebugComponent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            // 组件列表标题
            item {
                Text(
                    text = "组件列表",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            // 遍历渲染每个组件项
            items(components) { component ->
                val isSelected = component.id == selectedComponentId
                Surface(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                        .clickable { onComponentSelected(component) },
                    shape = RoundedCornerShape(8.dp),
                    // 选中态使用主色容器背景高亮
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(
                            text = component.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        // 组件描述信息，非空时才展示
                        if (component.description.isNotEmpty()) {
                            Text(
                                text = component.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 组件预览区。
 * 选中组件时展示组件名称头栏和实际内容，未选中时显示占位提示。
 *
 * @param component 当前选中的组件，为 null 时显示空状态
 * @param modifier 修饰符
 */
@Composable
private fun PreviewArea(
    component: DebugComponent?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        // 未选中组件时展示占位引导文字
        if (component == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "请从左侧列表选择一个组件进行预览",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // 组件头栏：名称和所属分组
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
                // 渲染实际组件内容，居中展示
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
}
