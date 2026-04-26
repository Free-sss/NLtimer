package com.nltimer.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.Tag
import com.nltimer.core.designsystem.theme.AppTheme
import com.nltimer.feature.home.model.GridCellUiState
import com.nltimer.feature.home.model.GridRowUiState
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.feature.home.model.TagUiState
import com.nltimer.feature.home.ui.components.TimeAxisGrid
import com.nltimer.feature.home.ui.components.TimeSideBar
import com.nltimer.feature.home.ui.sheet.AddBehaviorSheet
import java.time.LocalTime

/**
 * HomeScreen Composable - 显示主页的时间轴网格界面
 *
 * @param uiState 主页UI状态，包含加载状态、时间轴行数据等信息
 * @param activities 活动列表，用于在添加行为时显示
 * @param tagsForSelectedActivity 当前选中活动的标签列表
 * @param onEmptyCellClick 点击时间轴网格中的空单元格时的回调函数
 * @param onAddBehavior 添加行为时的回调函数，接收活动ID、标签ID列表、开始时间、行为性质和备注
 * @param onDismissSheet 关闭添加行为表单时的回调函数
 * @param onHourClick 点击小时侧边栏时的回调函数
 * @param modifier 可选的修饰符
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activities: List<Activity>,
    tagsForSelectedActivity: List<Tag>,
    onEmptyCellClick: () -> Unit,
    onAddBehavior: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, nature: BehaviorNature, note: String?) -> Unit,
    onDismissSheet: () -> Unit,
    onHourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Mark-style-main
    // 创建带有内边距的脚手架布局
    Scaffold(modifier = modifier) { padding ->
        if (uiState.isLoading) {
            // 显示加载指示器
            Box(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(padding)
                ,
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 显示时间轴网格和侧边栏
            Row(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(padding)
                    .padding(top = 0.dp, bottom = 0.dp)
                ,
            ) {
                TimeAxisGrid(
                    rows = uiState.rows,
                    onEmptyCellClick = onEmptyCellClick,
                    modifier = Modifier.weight(1f),
                )
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

        // 显示添加行为表单（如果可见）
        if (uiState.isAddSheetVisible) {
            AddBehaviorSheet(
                activities = activities,
                tagsForActivity = tagsForSelectedActivity,
                onDismiss = onDismissSheet,
                onConfirm = onAddBehavior,
            )
        }
    }
}
