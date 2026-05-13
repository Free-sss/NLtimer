package com.nltimer.core.data.model

enum class BehaviorNature(val key: String) {
    PENDING("pending"),
    ACTIVE("active"),
    COMPLETED("completed");

    val displaySymbol: String
        get() = when (this) {
            COMPLETED -> "✓"
            ACTIVE -> "▶"
            PENDING -> "○"
        }

    companion object {
        private val keyMap = entries.associateBy { it.key }
        fun fromKey(key: String): BehaviorNature = keyMap[key] ?: PENDING
    }
}
