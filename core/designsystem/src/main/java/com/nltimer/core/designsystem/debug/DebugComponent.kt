package com.nltimer.core.designsystem.debug

import androidx.compose.runtime.Composable

/**
 * 调试组件描述，用于在开发工具中注册并预览 UI 组件
 * @param id 唯一标识
 * @param name 显示名称
 * @param group 分组名称
 * @param description 组件功能说明
 * @param content 组件可组合内容
 */
data class DebugComponent(
    val id: String,
    val name: String,
    val group: String,
    val description: String = "",
    val implemented: Boolean = false,
    val content: @Composable () -> Unit,
)

/**
 * 调试组件注册中心，维护全局组件列表用于开发预览
 */
object DebugComponentRegistry {
    // 内部可变组件列表
    private val _components = mutableListOf<DebugComponent>()
    // 对外暴露不可变快照
    val components: List<DebugComponent> get() = _components.toList()

    /**
     * 注册一个调试组件到全局列表
     * @param component 待注册的调试组件
     */
    fun register(component: DebugComponent) {
        _components.add(component)
    }
}
