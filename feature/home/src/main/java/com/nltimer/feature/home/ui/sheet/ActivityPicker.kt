package com.nltimer.feature.home.ui.sheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.designsystem.icon.IconRenderer

/**
 * 活动选择器 Composable。
 * 将活动按分组展示为 FlowRow 可点选的标签网格。
 *
 * @param activities 全部活动列表
 * @param activityGroups 活动分组列表
 * @param selectedActivityId 当前选中的活动 ID
 * @param onActivitySelect 选中活动回调
 * @param modifier 修饰符
 */
@Composable
fun ActivityPicker(
    activities: List<Activity>,
    activityGroups: List<ActivityGroup>,
    selectedActivityId: Long?,
    onActivitySelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (activities.isEmpty()) return

    // 按分组将活动归类，未分组的放入"未分类"，并按排序顺序排列
    val groupedActivities = remember(activities, activityGroups) {
        val groupsMap = activityGroups.associateBy { it.id }
        activities.groupBy { it.groupId }
            .mapKeys { (groupId, _) ->
                if (groupId == null) "未分类" else groupsMap[groupId]?.name ?: "未知分类"
            }
            .toList()
            .sortedBy { (groupName, _) ->
                if (groupName == "未分类") {
                    Int.MAX_VALUE
                } else {
                    activityGroups.find { it.name == groupName }?.sortOrder ?: Int.MAX_VALUE
                }
            }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        groupedActivities.forEach { (groupName, groupActivities) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = groupName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )

                // FlowRow 排列每个分组下的活动按钮
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    for (activity in groupActivities) { key(activity.id) {
                        val isSelected = activity.id == selectedActivityId
                        val displayName = activity.name.let {
                            if (it.length > 5) it.take(5) + "..." else it
                        }

                        Surface(
                            onClick = { onActivitySelect(activity.id) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconRenderer(
                                    iconKey = activity.iconKey,
                                    defaultEmoji = "❓",
                                    iconSize = 20.dp,
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(
                                    text = displayName,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    } }
                }
            }
        }
    }
}
