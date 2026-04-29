package com.nltimer.feature.home.match

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 基于关键词大小写不敏感匹配的策略实现。
 * 对活动和标签名称进行模糊搜索过滤。
 */
@Singleton
class KeywordMatchStrategy @Inject constructor() : MatchStrategy {

    // 按活动名称模糊搜索，空查询返回全部
    override fun matchActivities(query: String, activities: List<Activity>): List<Activity> {
        if (query.isBlank()) return activities
        val lowerQuery = query.lowercase()
        return activities.filter { it.name.lowercase().contains(lowerQuery) }
    }

    // 按标签名称模糊搜索，空查询返回全部
    override fun matchTags(query: String, tags: List<Tag>): List<Tag> {
        if (query.isBlank()) return tags
        val lowerQuery = query.lowercase()
        return tags.filter { it.name.lowercase().contains(lowerQuery) }
    }
}
