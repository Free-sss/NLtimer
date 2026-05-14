package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.repository.ActivityManagementRepository
import javax.inject.Inject

/**
 * 添加活动用例。
 *
 * 封装 Activity 构造、插入和标签绑定逻辑，避免在多个 ViewModel 中重复。
 */
class AddActivityUseCase @Inject constructor(
    private val repository: ActivityManagementRepository,
) {
    suspend operator fun invoke(
        name: String,
        iconKey: String?,
        color: Long?,
        groupId: Long?,
        keywords: String?,
        tagIds: List<Long>,
    ): Long {
        val activity = Activity(
            name = name.trim(),
            iconKey = iconKey,
            color = color,
            groupId = groupId,
            isPreset = false,
            keywords = keywords,
        )
        val activityId = repository.addActivity(activity)
        if (tagIds.isNotEmpty()) {
            repository.setActivityTagBindings(activityId, tagIds)
        }
        return activityId
    }
}
