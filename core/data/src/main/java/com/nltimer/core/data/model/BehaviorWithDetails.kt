package com.nltimer.core.data.model

data class BehaviorWithDetails(
    val behavior: Behavior,
    val activity: Activity,
    val tags: List<Tag>,
)
