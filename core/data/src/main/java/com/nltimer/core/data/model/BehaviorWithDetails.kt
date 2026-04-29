package com.nltimer.core.data.model

/**
 * BehaviorWithDetails 行为完整详情模型
 * 关联行为记录、活动信息及其关联标签
 */
data class BehaviorWithDetails(
    val behavior: Behavior,
    val activity: Activity,
    val tags: List<Tag>,
)
