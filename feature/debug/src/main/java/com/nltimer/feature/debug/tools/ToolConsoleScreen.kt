package com.nltimer.feature.debug.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.core.tools.AccessLevel
import com.nltimer.core.tools.ParameterType
import com.nltimer.core.tools.ToolCategory
import com.nltimer.core.tools.ToolDefinition
import com.nltimer.core.tools.ToolError
import com.nltimer.core.tools.ToolParameter
import com.nltimer.core.tools.ToolResult

private const val MAX_PREVIEW = 1500

/**
 * 工具控制台屏 —— 三段式：
 * 1) 顶部 chips：按 [ToolCategory] 筛选
 * 2) 中部列表：可点开的工具卡片，展开后显示参数表单
 * 3) 底部结果区：上一次执行的 [ToolResult] 字符串展示
 *
 * 渲染入口由 `FeatureDebugComponents.registerAll()` 注册的 DebugComponent 触发，
 * 在 DebugPage 的 ModalBottomSheet 内显示。
 */
@Composable
fun ToolConsoleScreen(
    viewModel: ToolConsoleViewModel = hiltViewModel(),
) {
    val tools = viewModel.tools
    val result by viewModel.result.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    var selectedCategory by remember { mutableStateOf<ToolCategory?>(null) }
    var expandedTool by remember { mutableStateOf<String?>(null) }

    val visibleTools = remember(tools, selectedCategory) {
        if (selectedCategory == null) tools else tools.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 600.dp),
    ) {
        CategoryFilterRow(
            selected = selectedCategory,
            onSelect = { selectedCategory = it },
            availableCategories = remember(tools) { tools.map { it.category }.distinct() },
        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        if (tools.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(visibleTools, key = { it.name }) { tool ->
                    ToolCard(
                        tool = tool,
                        isExpanded = expandedTool == tool.name,
                        isRunning = isRunning,
                        onToggle = {
                            expandedTool = if (expandedTool == tool.name) null else tool.name
                            viewModel.clearResult()
                        },
                        onExecute = { args -> viewModel.execute(tool.name, args) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.padding(top = 8.dp))
        ResultPanel(result = result, isRunning = isRunning)
    }
}

@Composable
private fun CategoryFilterRow(
    selected: ToolCategory?,
    onSelect: (ToolCategory?) -> Unit,
    availableCategories: List<ToolCategory>,
) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scroll)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AssistChip(
            onClick = { onSelect(null) },
            label = { Text("全部") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selected == null) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
        )
        availableCategories.forEach { category ->
            AssistChip(
                onClick = { onSelect(category) },
                label = { Text(category.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == category) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                ),
            )
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolDefinition,
    isExpanded: Boolean,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onExecute: (Map<String, Any?>) -> Unit,
) {
    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpanded) "▼ ${tool.name}" else "▶ ${tool.name}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    modifier = Modifier.weight(1f),
                )
                AccessLevelTag(tool.accessLevel)
            }
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.padding(top = 12.dp))
                ToolForm(
                    parameters = tool.parameters,
                    isRunning = isRunning,
                    onExecute = onExecute,
                )
            }
        }
    }
}

@Composable
private fun AccessLevelTag(level: AccessLevel) {
    val (label, container) = when (level) {
        AccessLevel.NONE -> "NONE" to MaterialTheme.colorScheme.surface
        AccessLevel.READ -> "READ" to MaterialTheme.colorScheme.tertiaryContainer
        AccessLevel.WRITE -> "WRITE" to MaterialTheme.colorScheme.errorContainer
        AccessLevel.FULL -> "FULL" to MaterialTheme.colorScheme.errorContainer
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = container,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ToolForm(
    parameters: List<ToolParameter>,
    isRunning: Boolean,
    onExecute: (Map<String, Any?>) -> Unit,
) {
    val args = remember(parameters) { mutableStateMapOf<String, Any?>() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parameters.forEach { param ->
            ParameterField(param = param, onChange = { value -> args[param.name] = value })
        }

        Button(
            onClick = {
                // 仅传入用户实际输入过的参数；ToolRegistry 会做必填/约束校验
                onExecute(args.toMap())
            },
            enabled = !isRunning,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp,
                )
                Text("执行中…")
            } else {
                Text("执行")
            }
        }
    }
}

@Composable
private fun ParameterField(
    param: ToolParameter,
    onChange: (Any?) -> Unit,
) {
    var rawInput by remember(param.name) { mutableStateOf("") }
    var bool by remember(param.name) { mutableStateOf(false) }

    val label = buildString {
        append(param.name)
        if (param.required) append(" *")
        if (param.description.isNotBlank()) append("  —  ${param.description}")
    }

    when (param.type) {
        ParameterType.STRING -> {
            OutlinedTextField(
                value = rawInput,
                onValueChange = {
                    rawInput = it
                    onChange(it.takeIf { s -> s.isNotEmpty() })
                },
                label = { Text(label, fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ParameterType.NUMBER -> {
            OutlinedTextField(
                value = rawInput,
                onValueChange = {
                    rawInput = it
                    onChange(it.toLongOrNull() ?: it.toDoubleOrNull())
                },
                label = { Text(label, fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ParameterType.BOOLEAN -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp)
                Switch(
                    checked = bool,
                    onCheckedChange = {
                        bool = it
                        onChange(it)
                    },
                )
            }
        }

        else -> {
            // ARRAY / OBJECT / DATE_TIME / DURATION 暂用文本兜底，后续可扩展
            OutlinedTextField(
                value = rawInput,
                onValueChange = {
                    rawInput = it
                    onChange(it.takeIf { s -> s.isNotEmpty() })
                },
                label = { Text("$label  (${param.type})", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ResultPanel(
    result: ToolResult?,
    isRunning: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "执行结果",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.padding(top = 6.dp))
        when {
            isRunning -> Text("执行中…", style = MaterialTheme.typography.bodySmall)
            result == null -> Text(
                text = "（尚未执行）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> {
                val (bg, fg) = when (result) {
                    is ToolResult.Success -> {
                        MaterialTheme.colorScheme.tertiaryContainer to
                            MaterialTheme.colorScheme.onTertiaryContainer
                    }
                    is ToolResult.Error -> {
                        MaterialTheme.colorScheme.errorContainer to
                            MaterialTheme.colorScheme.onErrorContainer
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = bg,
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val tag = when (result) {
                            is ToolResult.Success -> "SUCCESS"
                            is ToolResult.Error -> "ERROR (${result.error.code})"
                        }
                        Text(
                            text = tag,
                            color = fg,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            ),
                        )
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        Text(
                            text = result.summarize(),
                            color = fg,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                            ),
                        )
                    }
                }
            }
        }
    }
}

private fun ToolResult.summarize(): String = when (this) {
    is ToolResult.Success -> "data = ${data.toString().take(MAX_PREVIEW)}"
    is ToolResult.Error -> when (val e = error) {
        is ToolError.ValidationError -> e.message
        is ToolError.PermissionDenied -> e.message
        is ToolError.NotFound -> e.message
        is ToolError.NetworkError -> e.message
        is ToolError.TimeoutError -> e.message
        is ToolError.InternalError -> e.message
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "尚无已注册工具",
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "在 core:tools 模块或各 feature 模块通过 @Binds @IntoSet 注册即可显示",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
