package com.nltimer.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.Tag
import com.nltimer.feature.home.model.HomeUiState
import com.nltimer.feature.home.ui.components.TimeAxisGrid
import com.nltimer.feature.home.ui.components.TimeSideBar
import com.nltimer.feature.home.ui.sheet.AddBehaviorSheet
import java.time.LocalTime

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activities: List<Activity>,
    tagsForSelectedActivity: List<Tag>,
    allTags: List<Tag>,
    onEmptyCellClick: () -> Unit,
    onAddBehavior: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, nature: BehaviorNature, note: String?) -> Unit,
    onDismissSheet: () -> Unit,
    onCompleteBehavior: (Long) -> Unit,
    onToggleIdleMode: () -> Unit,
    onStartNextPending: () -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit,
    onAddTag: (name: String) -> Unit,
    onHourClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        if (uiState.isLoading) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(padding)
                    .padding(top = 0.dp, bottom = 0.dp)
                ,
            ) {
                Row(modifier = Modifier.weight(1f)) {
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

                if (uiState.hasActiveBehavior) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    ) {
                        Button(
                            onClick = {
                                val activeBehaviorId = uiState.rows
                                    .flatMap { it.cells }
                                    .firstOrNull { it.isCurrent && it.behaviorId != null }
                                    ?.behaviorId
                                if (activeBehaviorId != null) {
                                    onCompleteBehavior(activeBehaviorId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("完成当前行为")
                        }
                    }
                }
            }
        }

        if (uiState.isAddSheetVisible) {
            AddBehaviorSheet(
                activities = activities,
                tagsForActivity = tagsForSelectedActivity,
                allTags = allTags,
                onDismiss = onDismissSheet,
                onConfirm = onAddBehavior,
                onAddActivity = onAddActivity,
                onAddTag = onAddTag,
            )
        }
    }
}
