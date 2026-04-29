package com.nltimer.feature.home.model

/**
 * 标签的 UI 展示状态。
 * 简化自领域模型 Tag，仅包含前端渲染所需字段。
 */
data class TagUiState(
    val id: Long, // 标签唯一 ID
    val name: String, // 标签显示名称
    val color: Long?, // 标签颜色 ARGB 值
    val isActive: Boolean = true, // 标签是否处于启用状态
)
