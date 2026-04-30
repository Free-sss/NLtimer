package com.nltimer.feature.home.model

import androidx.compose.runtime.Immutable
import com.nltimer.core.data.model.BehaviorNature
import java.time.LocalTime

/**
 * 网格视图中单个单元格的 UI 状态。
 * 表示一次行为记录的显示信息，或空白占位单元格。
 */
@Immutable
data class GridCellUiState(
    val behaviorId: Long?, // null 表示空白/占位单元格
    val activityEmoji: String?,
    val activityName: String?,
    val tags: List<TagUiState>,
    val status: BehaviorNature?,
    val isCurrent: Boolean, // 是否为正在进行的活跃行为
    val wasPlanned: Boolean = false, // 是否为预先计划的行为
    val achievementLevel: Int? = null,
    val estimatedDuration: Long? = null,
    val actualDuration: Long? = null,
    val durationMs: Long? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val isAddPlaceholder: Boolean = false,
    val note: String? = null,
)
