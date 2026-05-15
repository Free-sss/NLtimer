package com.nltimer.core.behaviorui.sheet

import androidx.compose.runtime.Immutable

enum class SortMode(val label: String) {
    FREQUENCY("频率"),
    ALPHA("字母"),
    RECENT("最近"),
}

@Immutable
data class CategoryGroup<T>(
    val id: Long,
    val name: String,
    val items: List<T>,
)

interface CategorizableItem {
    val itemId: Long
    val itemName: String
    val category: String?
    val usageCount: Int
    val lastUsedTimestamp: Long?
    val iconKey: String?
}
