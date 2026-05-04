package com.nltimer.feature.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.home.model.HomeUiState
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.LocalTheme
import com.nltimer.core.designsystem.theme.NLtimerTheme
import com.nltimer.feature.home.ui.components.BehaviorLogView
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.TagUiState
import com.nltimer.feature.home.ui.components.TimeAxisGrid
import com.nltimer.feature.home.ui.components.TimeSideBar
import com.nltimer.feature.home.ui.components.TimelineReverseView
import com.nltimer.feature.home.ui.sheet.AddBehaviorSheet
import java.time.LocalTime

/**
 * 首页主屏幕 Composable。
 * 根据当前布局模式渲染网格时间轴或时间线倒序视图。
 *
 * @param uiState 聚合的首页 UI 状态
 * @param activities 可选活动列表
 * @param activityGroups 活动分组列表
 * @param tagsForSelectedActivity 当前选中活动关联的标签
 * @param allTags 全部可用标签
 * @param onEmptyCellClick 点击空白单元格回调
 * @param onAddBehavior 添加行为回调
 * @param onDismissSheet 关闭底部弹窗回调
 * @param onCompleteBehavior 完成行为回调
 * @param onToggleIdleMode 切换空闲模式回调
 * @param onStartNextPending 开始下一个待办行为回调
 * @param onAddActivity 添加活动回调
 * @param onAddTag 添加标签回调
 * @param onHourClick 点击小时数回调
 * @param onLayoutChange 切换布局模式回调
 * @param dialogConfig 弹窗配置
 * @param modifier 修饰符
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForSelectedActivity: List<Tag>,
    allTags: List<Tag>,
    dialogConfig: DialogGridConfig = DialogGridConfig(),
    onEmptyCellClick: () -> Unit,
    onAddBehavior: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, nature: BehaviorNature, note: String?) -> Unit,
    onDismissSheet: () -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onToggleIdleMode: () -> Unit,
    onStartNextPending: () -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit,
    onAddTag: (name: String) -> Unit,
    onHourClick: (Int) -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 获取当前主题中保存的布局模式（网格或时间线）
    val layout = LocalTheme.current.homeLayout

    val activeBehaviorId by remember(uiState.rows) {
        derivedStateOf {
            uiState.rows
                .flatMap { it.cells }
                .firstOrNull { it.isCurrent && it.behaviorId != null }
                ?.behaviorId
        }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            MorphingFab(
                hasActiveBehavior = uiState.hasActiveBehavior,
                onAddClick = onEmptyCellClick,
                onCompleteClick = {
                    activeBehaviorId?.let { onCompleteBehavior(it) }
                },
            )
        }
    ) { padding ->
        // 加载中显示转圈指示器，否则渲染主内容
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                ,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp, bottom = 0.dp)
                ,
            ) {
                // 网格模式：左侧时间轴网格 + 右侧小时侧边栏
                when (layout) {
                    HomeLayout.GRID -> {
                        val showSideBar = LocalTheme.current.showTimeSideBar
                        Row(modifier = Modifier.weight(1f)) {
                            TimeAxisGrid(
                                rows = uiState.rows,
                                onEmptyCellClick = onEmptyCellClick,
                                currentHour = uiState.selectedTimeHour,
                                onLayoutChange = onLayoutChange,
                                showTimeSideBar = showSideBar,
                                modifier = Modifier.weight(1f),
                            )
                            if (showSideBar) {
                                TimeSideBar(
                                    activeHours = uiState.rows
                                        .filter { it.cells.any { cell -> cell.behaviorId != null } || it.isCurrentRow }
                                        .map { it.startTime.hour }
                                        .toSet(),
                                    currentHour = uiState.selectedTimeHour,
                                    onHourClick = onHourClick,
                                )
                            }
                        }
                    }
                    HomeLayout.TIMELINE_REVERSE -> {
                        TimelineReverseView(
                            cells = uiState.rows.flatMap { it.cells },
                            onAddClick = onEmptyCellClick,
                            onLayoutChange = onLayoutChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HomeLayout.LOG -> {
                        BehaviorLogView(
                            cells = uiState.rows.flatMap { it.cells },
                            onLayoutChange = onLayoutChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 显示添加行为的底部弹窗
        if (uiState.isAddSheetVisible) {
            AddBehaviorSheet(
                activities = activities,
                activityGroups = activityGroups,
                tagsForActivity = tagsForSelectedActivity,
                allTags = allTags,
                dialogConfig = dialogConfig,
                onDismiss = onDismissSheet,
                onConfirm = onAddBehavior,
                onAddActivity = onAddActivity,
                onAddTag = onAddTag,
            )
        }
    }
}

@Composable
private fun MorphingFab(
    hasActiveBehavior: Boolean,
    onAddClick: () -> Unit,
    onCompleteClick: () -> Unit,
) {
    val cornerRadius by animateDpAsState(
        targetValue = if (hasActiveBehavior) 16.dp else 28.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "fabCorner",
    )
    val containerColor by animateColorAsState(
        targetValue = if (hasActiveBehavior) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "fabContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (hasActiveBehavior) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "fabContentColor",
    )

    Surface(
        modifier = Modifier
            .offset(y = 24.dp, x = 4.dp)
            .animateContentSize(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                alignment = Alignment.CenterEnd,
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 0.dp,
        onClick = if (hasActiveBehavior) onCompleteClick else onAddClick,
    ) {
        AnimatedContent(
            targetState = hasActiveBehavior,
            transitionSpec = {
                fadeIn(animationSpec = tween(200, delayMillis = 100)) togetherWith
                    fadeOut(animationSpec = tween(100))
            },
            label = "fabContent",
        ) { hasActive ->
            if (hasActive) {
                Text(
                    text = "完成当前行为",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加行为",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

// 预览用示例数据
@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val sampleTags = listOf(
        Tag(1, "Tag 1", null, null, null, null, 0, 0, 0, false),
        Tag(2, "Tag 2", null, null, null, null, 0, 0, 0, false)
    )
    val sampleActivities = listOf(
        Activity(1, "Activity 1", "😊", null, null, false),
        Activity(2, "Activity 2", "🚀", null, null, false)
    )
    val sampleUiState = HomeUiState(
        isLoading = false,
        rows = listOf(
            GridRowUiState(
                rowId = "1",
                startTime = LocalTime.of(9, 0),
                isCurrentRow = true,
                isLocked = false,
                cells = listOf(
                    GridCellUiState(
                        behaviorId = 1L,
                        activityEmoji = "😊",
                        activityName = "Activity 1",
                        tags = listOf(TagUiState(1, "Tag 1", null)),
                        status = BehaviorNature.ACTIVE,
                        isCurrent = true
                    )
                )
            )
        ),
        selectedTimeHour = 9
    )

    NLtimerTheme {
        HomeScreen(
            uiState = sampleUiState,
            activities = sampleActivities,
            activityGroups = emptyList(),
            tagsForSelectedActivity = sampleTags,
            allTags = sampleTags,
            onEmptyCellClick = {},
            onAddBehavior = { _, _, _, _, _ -> },
            onDismissSheet = {},
            onCompleteBehavior = {},
            onToggleIdleMode = {},
            onStartNextPending = {},
            onAddActivity = { _, _ -> },
            onAddTag = {},
            onHourClick = {},
            onLayoutChange = {}
        )
    }
}
