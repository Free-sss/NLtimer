package com.nltimer.feature.home.match

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag

/**
 * 搜索匹配策略接口。
 * 定义活动和标签的匹配行为，支持依赖注入替换实现。
 */
interface MatchStrategy {
    // 根据查询文本匹配活动列表
    fun matchActivities(query: String, activities: List<Activity>): List<Activity>
    // 根据查询文本匹配标签列表
    fun matchTags(query: String, tags: List<Tag>): List<Tag>
}
