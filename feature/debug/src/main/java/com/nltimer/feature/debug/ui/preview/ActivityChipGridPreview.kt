package com.nltimer.feature.debug.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ActivityChipData(
    val name: String,
    val color: Color
)

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
        maxItemsInEachRow = 6
    ) {
        activities.forEach { activity ->
            ActivityChip(
                activity = activity,
                onClick = { onActivityClick(activity) }
            )
        }

        FunctionChip(
            label = "管理",
            icon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "管理",
                    modifier = Modifier.size(18.dp)
                )
            },
            containerColor = Color(0xFF616161).copy(alpha = 0.15f),
            contentColor = Color(0xFF616161).copy(alpha = 0.9f),
            borderColor = Color(0xFF616161).copy(alpha = 0.5f),
            onClick = onManageClick
        )

        FunctionChip(
            label = "新增",
            icon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "新增",
                    modifier = Modifier.size(18.dp)
                )
            },
            containerColor = Color(0xFFFF5252).copy(alpha = 0.15f),
            contentColor = Color(0xFFFF5252).copy(alpha = 0.9f),
            borderColor = Color(0xFFFF5252).copy(alpha = 0.5f),
            onClick = onAddClick
        )
    }
}

@Composable
private fun ActivityChip(
    activity: ActivityChipData,
    onClick: () -> Unit
) {
    val containerColor = activity.color.copy(alpha = 0.15f)
    val contentColor = activity.color.copy(alpha = 0.9f)
    val borderColor = activity.color.copy(alpha = 0.5f)

    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = activity.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 0.dp)
            )
        },
        modifier = Modifier.height(28.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            iconContentColor = contentColor
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = borderColor,
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(6.dp)
    )
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
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 0.dp)
            )
        },
        icon = icon,
        modifier = Modifier.height(28.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            iconContentColor = contentColor
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = borderColor,
            borderWidth = 1.dp
        ),
        shape = RoundedCornerShape(6.dp)
    )
}
