package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.model.Tag
import java.time.LocalTime

/**
 * 添加行为的 ModalBottomSheet Composable。
 * 包含活动选择、标签选择、时间选择、类型选择和备注输入。
 *
 * @param modifier 修饰符
 * @param activities 全部活动列表
 * @param activityGroups 活动分组列表
 * @param tagsForActivity 当前选中活动关联的标签
 * @param allTags 全部可用标签
 * @param onDismiss 关闭弹窗回调
 * @param onConfirm 确认添加行为回调
 * @param onAddActivity 添加活动回调
 * @param onAddTag 添加标签回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBehaviorSheet(
    modifier: Modifier = Modifier,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForActivity: List<Tag>,
    allTags: List<Tag> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit = { _, _ -> },
    onAddTag: (name: String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        AddBehaviorSheetContent(
            modifier = modifier,
            activities = activities,
            activityGroups = activityGroups,
            tagsForActivity = tagsForActivity,
            allTags = allTags,
            onConfirm = { activityId, tagIds, startTime, nature, note ->
                onConfirm(activityId, tagIds, startTime, nature, note)
                onDismiss()
            },
            onAddActivity = onAddActivity,
            onAddTag = onAddTag,
        )
    }
}

/**
 * 添加行为底部弹窗的内容主体 Composable（不含 ModalBottomSheet 壳）。
 * 分离出来便于预览和测试。
 */
@Composable
fun AddBehaviorSheetContent(
    modifier: Modifier = Modifier,
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    tagsForActivity: List<Tag>,
    allTags: List<Tag> = emptyList(),
    onConfirm: (activityId: Long, tagIds: List<Long>, startTime: LocalTime, nature: BehaviorNature, note: String?) -> Unit,
    onAddActivity: (name: String, emoji: String) -> Unit = { _, _ -> },
    onAddTag: (name: String) -> Unit = {},
) {
    // 表单本地状态：选中的活动、标签、时间、类型和备注
    var selectedActivityId by remember { mutableStateOf<Long?>(null) }
    var selectedTagIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var startTime by remember { mutableStateOf(LocalTime.now()) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var nature by remember { mutableStateOf(BehaviorNature.ACTIVE) }
    var note by remember { mutableStateOf("") }

    // 控制添加活动和标签的弹出对话框
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp),
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // "活动"标题行，右侧有"+ 添加"快速入口
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAddActivityDialog = true }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "活动",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "+ 添加",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        ActivityPicker(
            activities = activities,
            activityGroups = activityGroups,
            selectedActivityId = selectedActivityId,
            onActivitySelect = { selectedActivityId = it },
        )

        // 选中活动且有标签时显示关联标签选择
        if (selectedActivityId != null && tagsForActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAddTagDialog = true }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "关联标签",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "+ 添加",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TagPicker(
                tags = tagsForActivity,
                selectedTagIds = selectedTagIds,
                onTagToggle = { tagId ->
                    selectedTagIds = if (tagId in selectedTagIds) {
                        selectedTagIds - tagId
                    } else {
                        selectedTagIds + tagId
                    }
                },
            )
        }

        // "所有标签"区域，独立显示全部标签供选择
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAddTagDialog = true }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "所有标签",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "+ 添加",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TagPicker(
            tags = allTags,
            selectedTagIds = selectedTagIds,
            onTagToggle = { tagId ->
                selectedTagIds = if (tagId in selectedTagIds) {
                    selectedTagIds - tagId
                } else {
                    selectedTagIds + tagId
                }
            },
        )

        // 备注输入
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "备注 (可选)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        NoteInput(
            note = note,
            onNoteChange = { note = it },
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column (
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ACTIVE 和 COMPLETED 类型才显示开始时间（COMPLETED 额外显示结束时间）
                if (nature == BehaviorNature.ACTIVE || nature == BehaviorNature.COMPLETED) {
                    Text(
                        "开始",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TimePickerCompact(
                        time = startTime,
                        onTimeChange = { startTime = it },
                    )
                    if(nature == BehaviorNature.COMPLETED){
                        Text(
                            "结束",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TimePickerCompact(
                            time = endTime,
                            onTimeChange = { endTime = it },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "类型",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BehaviorNatureSelector(
                    selected = nature,
                    onSelect = { nature = it },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 提交按钮：选中活动后启用
        Button(
            onClick = {
                selectedActivityId?.let { activityId ->
                    onConfirm(activityId, selectedTagIds.toList(), startTime, nature, note.ifBlank { null })
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = selectedActivityId != null,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("完成", color = MaterialTheme.colorScheme.onPrimary)
        }
    }

    // 弹出添加活动对话框
    if (showAddActivityDialog) {
        AddActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onConfirm = { name, emoji ->
                onAddActivity(name, emoji)
                showAddActivityDialog = false
            },
        )
    }

    // 弹出添加标签对话框
    if (showAddTagDialog) {
        AddTagDialog(
            onDismiss = { showAddTagDialog = false },
            onConfirm = { name ->
                onAddTag(name)
                showAddTagDialog = false
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddBehaviorSheetPreview() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        AddBehaviorSheetContent(
            activities = listOf(
                Activity(1, "Coding", "👨‍💻", null, null, false),
                Activity(2, "Reading", "📚", null, null, false),
                Activity(3, "Workout", "💪", null, null, false)
            ),
            activityGroups = emptyList(),
            tagsForActivity = listOf(
                Tag(1, "Work", null, null, null, null, 0, 0, 0, false),
                Tag(2, "Study", null, null, null, null, 0, 0, 0, false)
            ),
            allTags = listOf(
                Tag(1, "Work", null, null, null, null, 0, 0, 0, false),
                Tag(2, "Study", null, null, null, null, 0, 0, 0, false),
                Tag(3, "Health", null, null, null, null, 0, 0, 0, false)
            ),
            onConfirm = { _, _, _, _, _ -> }
        )
    }
}
