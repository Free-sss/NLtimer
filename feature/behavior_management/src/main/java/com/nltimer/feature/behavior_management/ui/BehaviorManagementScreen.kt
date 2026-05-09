package com.nltimer.feature.behavior_management.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.behaviorui.sheet.AddBehaviorSheet
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.BehaviorWithDetails
import com.nltimer.feature.behavior_management.model.DuplicateHandling
import com.nltimer.feature.behavior_management.model.ViewMode
import com.nltimer.feature.behavior_management.viewmodel.BehaviorManagementViewModel
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BehaviorManagementScreen(
    viewModel: BehaviorManagementViewModel,
    onNavigateBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activityGroups by viewModel.activityGroups.collectAsStateWithLifecycle()
    val tagCategories by viewModel.tagCategories.collectAsStateWithLifecycle()
    val activities by viewModel.allActivities.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    var selectedHandling by remember { mutableStateOf(DuplicateHandling.SKIP) }

    val editBehavior = remember(uiState.editBehaviorId, uiState.behaviors) {
        uiState.behaviors.find { it.behavior.id == uiState.editBehaviorId }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (uiState.isMultiSelectMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "已选 ${uiState.selectedBehaviorIds.size} 项",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = viewModel::exitMultiSelect) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onImport) {
                        Icon(Icons.Default.FileDownload, contentDescription = "导入")
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Default.FileUpload, contentDescription = "导出")
                    }
                }
            }
            TimeRangeSelector(
                currentPreset = uiState.timeRange,
                currentDate = uiState.rangeStartDate,
                onPresetChange = viewModel::setTimeRange,
                onDateChange = viewModel::setRangeStartDate,
                onNavigate = viewModel::navigateRange,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            FilterBar(
                activityGroups = activityGroups.map { it.name },
                tagCategories = tagCategories,
                selectedActivityGroup = uiState.selectedActivityGroup,
                selectedTagCategory = uiState.selectedTagCategory,
                selectedStatus = uiState.selectedStatus,
                searchQuery = uiState.searchQuery,
                onActivityGroupChange = viewModel::setActivityGroupFilter,
                onTagCategoryChange = viewModel::setTagCategoryFilter,
                onStatusChange = viewModel::setStatusFilter,
                onSearchQueryChange = viewModel::setSearchQuery,
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FilterChip(
                    selected = uiState.viewMode == ViewMode.LIST,
                    onClick = { viewModel.setViewMode(ViewMode.LIST) },
                    label = { Text("列表", style = MaterialTheme.typography.labelMedium) },
                )
                FilterChip(
                    selected = uiState.viewMode == ViewMode.TIMELINE,
                    onClick = { viewModel.setViewMode(ViewMode.TIMELINE) },
                    label = { Text("时间轴", style = MaterialTheme.typography.labelMedium) },
                )
            }

            if (uiState.behaviors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无行为记录",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    if (uiState.viewMode == ViewMode.LIST) {
                        itemsIndexed(
                            items = uiState.behaviors,
                            key = { _, item -> item.behavior.id },
                        ) { index, item ->
                            BehaviorListItem(
                                behaviorWithDetails = item,
                                isSelected = item.behavior.id in uiState.selectedBehaviorIds,
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleMultiSelect(item.behavior.id)
                                    } else {
                                        viewModel.startEditBehavior(item.behavior.id)
                                    }
                                },
                                onLongClick = { viewModel.toggleMultiSelect(item.behavior.id) },
                                isEvenItem = index % 2 == 0,
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = uiState.behaviors,
                            key = { _, item -> item.behavior.id },
                        ) { index, item ->
                            BehaviorTimelineItem(
                                behaviorWithDetails = item,
                                isLast = index == uiState.behaviors.lastIndex,
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleMultiSelect(item.behavior.id)
                                    } else {
                                        viewModel.startEditBehavior(item.behavior.id)
                                    }
                                },
                                onLongClick = { viewModel.toggleMultiSelect(item.behavior.id) },
                            )
                        }
                    }
                }

                SummaryBar(
                    behaviors = uiState.behaviors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    editBehavior?.let { bwd ->
        val initialStartTime = Instant.ofEpochMilli(bwd.behavior.startTime)
            .atZone(ZoneId.systemDefault()).toLocalTime()
        val initialEndTime = bwd.behavior.endTime?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
        }

        AddBehaviorSheet(
            activities = activities,
            activityGroups = activityGroups,
            tagsForActivity = emptyList(),
            allTags = allTags,
            initialStartTime = initialStartTime,
            initialEndTime = initialEndTime,
            initialActivityId = bwd.activity.id,
            initialTagIds = bwd.tags.map { it.id },
            initialNote = bwd.behavior.note,
            editBehaviorId = bwd.behavior.id,
            existingBehaviors = uiState.behaviors.map { it.behavior },
            onDismiss = viewModel::finishEditBehavior,
            onConfirm = { activityId, tagIds, startTime, endTime, nature, note ->
                viewModel.updateBehavior(
                    bwd.behavior.id, activityId, tagIds, startTime, endTime, nature, note,
                )
            },
        )
    }

    uiState.importPreview?.let { preview ->
        ImportPreviewDialog(
            preview = preview,
            onDuplicateHandlingChange = { selectedHandling = it },
            selectedHandling = selectedHandling,
            onConfirm = {
                uiState.importData?.let { data ->
                    viewModel.executeImport(data, selectedHandling)
                }
            },
            onDismiss = viewModel::dismissImportPreview,
        )
    }
}

@Composable
private fun SummaryBar(
    behaviors: List<BehaviorWithDetails>,
    modifier: Modifier = Modifier,
) {
    val totalCount = behaviors.size
    val completedCount = behaviors.count { it.behavior.status == BehaviorNature.COMPLETED }
    val totalDurationMinutes = behaviors
        .filter { it.behavior.endTime != null }
        .sumOf { bwd ->
            (bwd.behavior.endTime!! - bwd.behavior.startTime) / 60_000
        }
    val hours = totalDurationMinutes / 60
    val minutes = totalDurationMinutes % 60
    val durationText = if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "共 $totalCount 条",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Text(
            text = "已完成 $completedCount",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Text(
            text = "总时长 $durationText",
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
