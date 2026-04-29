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

@Composable
fun DebugPage() {
    val allComponents = remember { DebugComponentRegistry.components }
    val groups = remember(allComponents) {
        listOf("全部") + allComponents.map { it.group }.distinct()
    }

    var selectedGroup by remember { mutableStateOf("全部") }
    var selectedComponentId by remember { mutableStateOf<String?>(null) }

    val filteredComponents = remember(selectedGroup, allComponents) {
        if (selectedGroup == "全部") allComponents
        else allComponents.filter { it.group == selectedGroup }
    }

    val selectedComponent = remember(selectedComponentId, allComponents) {
        allComponents.find { it.id == selectedComponentId }
    }

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
            item {
                Text(
                    text = "分组",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            items(groups) { group ->
                val isSelected = group == selectedGroup
                Surface(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                        .clickable { onGroupSelected(group) },
                    shape = RoundedCornerShape(8.dp),
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
            item {
                Text(
                    text = "组件列表",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            items(components) { component ->
                val isSelected = component.id == selectedComponentId
                Surface(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .fillMaxWidth()
                        .clickable { onComponentSelected(component) },
                    shape = RoundedCornerShape(8.dp),
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
