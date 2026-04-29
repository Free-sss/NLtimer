package com.nltimer.core.designsystem.debug

import androidx.compose.runtime.Composable

data class DebugComponent(
    val id: String,
    val name: String,
    val group: String,
    val description: String = "",
    val content: @Composable () -> Unit,
)

object DebugComponentRegistry {
    private val _components = mutableListOf<DebugComponent>()
    val components: List<DebugComponent> get() = _components.toList()

    fun register(component: DebugComponent) {
        _components.add(component)
    }
}
