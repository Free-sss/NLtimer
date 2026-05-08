package com.nltimer.core.data.model

/**
 * 添加标签回调签名。
 *
 * 用于表单中添加标签时的 onConfirm 参数，减少重复的7参数lambda声明。
 */
typealias AddTagCallback = (name: String, color: Long?, icon: String?, priority: Int, category: String?, keywords: String?, activityId: Long?) -> Unit

/**
 * 添加活动回调签名。
 *
 * 用于表单中添加活动时的 onConfirm 参数，减少重复的6参数lambda声明。
 */
typealias AddActivityCallback = (name: String, iconKey: String?, color: Long?, groupId: Long?, keywords: String?, tagIds: List<Long>) -> Unit
