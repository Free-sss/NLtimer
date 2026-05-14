package com.nltimer.feature.debug.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.core.tools.ToolDefinition
import com.nltimer.core.tools.ToolRegistry
import com.nltimer.core.tools.ToolResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 工具控制台 ViewModel
 *
 * - 启动时从 [ToolRegistry] 读取已注册工具快照
 * - [execute] 把参数透传给 ToolRegistry，将结果回写到 [result]
 * - [clearResult] 让 UI 关闭弹窗或切换工具时清空上一轮结果
 *
 * 注意：[tools] 是构造时一次性快照（debug 入口生命周期内不会改变），
 * 如未来支持运行时动态注册工具，可改为 StateFlow。
 */
@HiltViewModel
class ToolConsoleViewModel @Inject constructor(
    private val toolRegistry: ToolRegistry,
) : ViewModel() {

    val tools: List<ToolDefinition> = toolRegistry.getAllTools()

    private val _result = MutableStateFlow<ToolResult?>(null)
    val result: StateFlow<ToolResult?> = _result.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun execute(toolName: String, args: Map<String, Any?>) {
        viewModelScope.launch {
            _isRunning.value = true
            try {
                _result.value = toolRegistry.executeTool(toolName, args)
            } finally {
                _isRunning.value = false
            }
        }
    }

    fun clearResult() {
        _result.value = null
    }
}
