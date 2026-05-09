package com.nltimer.feature.behavior_management.model

enum class TimeRangePreset(val label: String, val hours: Long) {
    FOUR_HOURS("4小时", 4),
    EIGHT_HOURS("8小时", 8),
    ONE_DAY("1日", 24),
    THREE_DAYS("3日", 72),
    SEVEN_DAYS("7日", 168),
    ONE_MONTH("1月", 720),
    ONE_YEAR("1年", 8760),
}
