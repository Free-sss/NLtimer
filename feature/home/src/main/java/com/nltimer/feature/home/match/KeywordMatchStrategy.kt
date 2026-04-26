package com.nltimer.feature.home.match

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeywordMatchStrategy @Inject constructor() : MatchStrategy {

    override fun matchActivities(query: String, activities: List<Activity>): List<Activity> {
        if (query.isBlank()) return activities
        val lowerQuery = query.lowercase()
        return activities.filter { it.name.lowercase().contains(lowerQuery) }
    }

    override fun matchTags(query: String, tags: List<Tag>): List<Tag> {
        if (query.isBlank()) return tags
        val lowerQuery = query.lowercase()
        return tags.filter { it.name.lowercase().contains(lowerQuery) }
    }
}
