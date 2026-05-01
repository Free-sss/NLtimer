package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nltimer.core.designsystem.theme.NLtimerTheme

enum class ChipDisplayMode {
    Filled,
    Underline,
    
}

data class ActivityChipData(
    val name: String,
    val color: Color
)

@Preview(showBackground = true)
@Composable
fun ActivityChipGridDebugPreview() {
    val sampleActivities = listOf(
        ActivityChipData("学习", Color(0xFF1B5E20)),
        ActivityChipData("读书", Color(0xFF43A047)),
        ActivityChipData("英语", Color(0xFF66BB6A)),
        ActivityChipData("c语言", Color(0xFF81C784)),
        ActivityChipData("微积分", Color(0xFF757575)),
        ActivityChipData("休息", Color(0xFF212121)),
        ActivityChipData("睡觉", Color(0xFF00BFA5)),
        ActivityChipData("Take a nap", Color(0xFF4DB6AC)),
        ActivityChipData("冥想", Color(0xFF006064)),
        ActivityChipData("信息流", Color(0xFFB71C1C)),
        ActivityChipData("生活", Color(0xFF8D6E63)),
        ActivityChipData("厕所", Color(0xFFD7CCC8)),
        ActivityChipData("金刚功", Color(0xFF795548)),
        ActivityChipData("吃饭", Color(0xFFD2B48C)),
        ActivityChipData("多巴胺", Color(0xFFB8860B))
    )

    NLtimerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                ActivityGridComponent(
                    activities = sampleActivities,
                    onActivityClick = { },
                    onManageClick = { },
                    onAddClick = { }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActivityGridComponent(
    activities: List<ActivityChipData>,
    onActivityClick: (ActivityChipData) -> Unit,
    onManageClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
//        maxItemsInEachRow = 6
    ) {
        FunctionChip(
            label = "活动管理",
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "管理",
                    modifier = Modifier.size(14.dp)
                )
            },
            containerColor = Color.Transparent,
            contentColor = Color(0xFF616161).copy(alpha = 0.9f),
            borderColor = Color.Transparent,
            onClick = onManageClick
        )
        activities.forEach { activity ->
            AdaptiveActivityChip(
                activity = activity,
                displayMode = ChipDisplayMode.Underline ,
                onClick = { onActivityClick(activity) }
            )
        }




    }
}

@Composable
private fun AdaptiveActivityChip(
    activity: ActivityChipData,
    displayMode: ChipDisplayMode,
    onClick: () -> Unit
) {
    val containerColor = activity.color.copy(alpha = 0.15f)
    val contentColor = activity.color.copy(alpha = 0.9f)

    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .height(24.dp)
                .widthIn(max = 100.dp)
                .then(
                    if (displayMode == ChipDisplayMode.Underline) {
                        Modifier.drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            val y = size.height - strokeWidth / 2
                            drawLine(
                                color = containerColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth,
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            color = if (displayMode == ChipDisplayMode.Filled) containerColor else Color.Transparent,
            contentColor = contentColor,
            shape = RoundedCornerShape(6.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = activity.name,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FunctionChip(
    label: String,
    icon: @Composable (() -> Unit)? = null,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(24.dp),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(16.dp)) { icon() }
            }
            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityChipPreview() {
    NLtimerTheme {
        AdaptiveActivityChip(
            activity = ActivityChipData("学习", Color(0xFF1B5E20)),
            displayMode = ChipDisplayMode.Filled,
            onClick = { }
        )
    }
}
