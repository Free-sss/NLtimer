package com.nltimer.feature.behavior_management.model

data class ImportPreview(
    val totalCount: Int,
    val duplicateCount: Int,
    val newCount: Int,
    val duplicateItems: List<ImportPreviewItem>,
    val newItems: List<ImportNewItem>,
)

data class ImportPreviewItem(
    val activityName: String,
    val startTime: Long,
    val endTime: Long?,
)

data class ImportNewItem(
    val type: NewItemType,
    val name: String,
)

enum class NewItemType { ACTIVITY, TAG }

enum class DuplicateHandling {
    SKIP, OVERWRITE, ALLOW,
}
