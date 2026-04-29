package com.nltimer.feature.home.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.home.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * 首页路由入口 Composable。
 * 负责从 [HomeViewModel] 收集状态并转发给 [HomeScreen]。
 *
 * @param viewModel Hilt 注入的首页 ViewModel
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    // 收集 ViewModel 中的各类状态流
    val uiState by viewModel.uiState.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val activityGroups by viewModel.activityGroups.collectAsState()
    val tagsForSelectedActivity by viewModel.tagsForSelectedActivity.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    // 将 LocalTime 转为毫秒时间戳后再调用 ViewModel
    HomeScreen(
        uiState = uiState,
        activities = activities,
        activityGroups = activityGroups,
        tagsForSelectedActivity = tagsForSelectedActivity,
        allTags = allTags,
        onEmptyCellClick = viewModel::showAddSheet,
        onAddBehavior = { activityId, tagIds, startTime, nature, note ->
            val epochMillis = LocalDate.now()
                .atTime(startTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            viewModel.addBehavior(activityId, tagIds, epochMillis, nature, note)
        },
        onDismissSheet = viewModel::hideAddSheet,
        onCompleteBehavior = { viewModel.completeBehavior(it) },
        onToggleIdleMode = viewModel::toggleIdleMode,
        onStartNextPending = viewModel::startNextPending,
        onAddActivity = { name, emoji -> viewModel.addActivity(name, emoji) },
        onAddTag = { name -> viewModel.addTag(name) },
        onHourClick = viewModel::scrollToTime,
        onLayoutChange = viewModel::onHomeLayoutChange,
    )
}
