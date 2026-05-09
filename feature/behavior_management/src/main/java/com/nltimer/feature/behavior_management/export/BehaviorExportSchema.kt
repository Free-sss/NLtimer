package com.nltimer.feature.behavior_management.export

data class BehaviorExportData(
    val version: Int = 1,
    val exportedAt: Long,
    val timeRange: TimeRangeInfo?,
    val filters: FilterInfo?,
    val behaviors: List<BehaviorExportItem>,
)

data class TimeRangeInfo(
    val start: String,
    val end: String,
    val label: String,
)

data class FilterInfo(
    val activityGroup: String? = null,
    val tagCategory: String? = null,
    val status: String? = null,
)

data class BehaviorExportItem(
    val startTime: Long,
    val endTime: Long? = null,
    val status: String,
    val note: String? = null,
    val pomodoroCount: Int = 0,
    val sequence: Int = 0,
    val estimatedDuration: Long? = null,
    val actualDuration: Long? = null,
    val achievementLevel: Int? = null,
    val wasPlanned: Boolean = false,
    val activity: ActivityExportItem,
    val tags: List<TagExportItem> = emptyList(),
)

data class ActivityExportItem(
    val name: String,
    val iconKey: String? = null,
    val color: Long? = null,
)

data class TagExportItem(
    val name: String,
    val color: Long? = null,
)
