package com.nltimer.feature.home.match

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag

interface MatchStrategy {
    fun matchActivities(query: String, activities: List<Activity>): List<Activity>
    fun matchTags(query: String, tags: List<Tag>): List<Tag>
}
