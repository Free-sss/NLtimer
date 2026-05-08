package com.nltimer.feature.debug.ui.components

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag

/**
 * 字段信息数据类，用于描述单个字段的调试信息
 *
 * @param name 字段名，如 "name", "iconKey"
 * @param displayName 显示名，如 "名称", "图标"
 * @param value 字段值
 * @param isDisplayed 是否在 UI 上展示
 * @param isMissing 是否缺失（null/空/默认值）
 */
data class FieldInfo(
    val name: String,
    val displayName: String,
    val value: Any?,
    val isDisplayed: Boolean,
    val isMissing: Boolean,
)

/**
 * 判断字段值是否为缺失状态
 * - null 值视为缺失
 * - 空字符串视为缺失
 * - Int/Long 类型的 0 视为缺失（排除 id 字段）
 * - Boolean 类型的 false 视为缺失
 */
fun Any?.isFieldMissing(fieldName: String): Boolean = when (this) {
    null -> true
    is String -> isBlank()
    is Int -> this == 0 && !fieldName.endsWith("Id")
    is Long -> this == 0L && !fieldName.endsWith("Id")
    is Boolean -> !this
    else -> false
}

/**
 * 将 Activity 对象转换为 FieldInfo 列表
 */
fun Activity.toFieldInfoList(): List<FieldInfo> = listOf(
    FieldInfo("id", "ID", id, isDisplayed = false, isMissing = id == 0L),
    FieldInfo("name", "名称", name, isDisplayed = true, isMissing = name.isFieldMissing("name")),
    FieldInfo("iconKey", "图标", iconKey, isDisplayed = true, isMissing = iconKey.isFieldMissing("iconKey")),
    FieldInfo("color", "颜色", color, isDisplayed = true, isMissing = color.isFieldMissing("color")),
    FieldInfo("keywords", "关键词", keywords, isDisplayed = false, isMissing = keywords.isFieldMissing("keywords")),
    FieldInfo("groupId", "分组", groupId, isDisplayed = false, isMissing = groupId.isFieldMissing("groupId")),
    FieldInfo("isPreset", "预设", isPreset, isDisplayed = false, isMissing = false),
    FieldInfo("isArchived", "归档", isArchived, isDisplayed = false, isMissing = false),
    FieldInfo("archivedAt", "归档时间", archivedAt, isDisplayed = false, isMissing = archivedAt.isFieldMissing("archivedAt")),
    FieldInfo("usageCount", "使用次数", usageCount, isDisplayed = true, isMissing = usageCount.isFieldMissing("usageCount")),
)

/**
 * 将 Tag 对象转换为 FieldInfo 列表
 */
fun Tag.toFieldInfoList(): List<FieldInfo> = listOf(
    FieldInfo("id", "ID", id, isDisplayed = false, isMissing = id == 0L),
    FieldInfo("name", "名称", name, isDisplayed = true, isMissing = name.isFieldMissing("name")),
    FieldInfo("color", "颜色", color, isDisplayed = true, isMissing = color.isFieldMissing("color")),
    FieldInfo("iconKey", "图标", iconKey, isDisplayed = true, isMissing = iconKey.isFieldMissing("iconKey")),
    FieldInfo("category", "分类", category, isDisplayed = false, isMissing = category.isFieldMissing("category")),
    FieldInfo("priority", "优先级", priority, isDisplayed = false, isMissing = priority.isFieldMissing("priority")),
    FieldInfo("usageCount", "使用次数", usageCount, isDisplayed = true, isMissing = usageCount.isFieldMissing("usageCount")),
    FieldInfo("sortOrder", "排序", sortOrder, isDisplayed = false, isMissing = false),
    FieldInfo("keywords", "关键词", keywords, isDisplayed = false, isMissing = keywords.isFieldMissing("keywords")),
    FieldInfo("isArchived", "归档", isArchived, isDisplayed = false, isMissing = false),
    FieldInfo("archivedAt", "归档时间", archivedAt, isDisplayed = false, isMissing = archivedAt.isFieldMissing("archivedAt")),
)
