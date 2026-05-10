package com.nltimer.feature.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.data.model.SecondsStrategy
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.GridLayoutMode
import com.nltimer.core.designsystem.theme.PathDrawMode

@Composable
fun DialogConfigRoute(
    viewModel: DialogConfigViewModel = hiltViewModel(),
) {
    val config by viewModel.dialogConfig.collectAsState()
    DialogConfigScreen(
        config = config,
        onUpdateConfig = viewModel::updateConfig,
    )
}

@Composable
fun DialogConfigScreen(
    config: DialogGridConfig,
    onUpdateConfig: (DialogGridConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ConfigExpandableSection(title = "活动配置") {
                ChipSelectorRow(
                    label = "样式",
                    options = ChipDisplayMode.entries,
                    selected = config.activityDisplayMode,
                    display = { it.displayName() },
                    onSelect = { onUpdateConfig(config.copy(activityDisplayMode = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChipSelectorRow(
                    label = "布局",
                    options = GridLayoutMode.entries,
                    selected = config.activityLayoutMode,
                    display = { it.displayName() },
                    onSelect = { onUpdateConfig(config.copy(activityLayoutMode = it)) },
                )
                if (config.activityLayoutMode == GridLayoutMode.Vertical) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepperControl(
                        label = "每列行数",
                        value = config.activityColumnLines,
                        min = 1,
                        max = 10,
                        onValueChange = { onUpdateConfig(config.copy(activityColumnLines = it)) },
                    )
                }
                if (config.activityLayoutMode == GridLayoutMode.Horizontal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepperControl(
                        label = "最多行数（0=无限）",
                        value = config.activityHorizontalLines,
                        min = 0,
                        max = 10,
                        infiniteAtMin = true,
                        onValueChange = { onUpdateConfig(config.copy(activityHorizontalLines = it)) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ToggleControl(
                    label = "文字配色",
                    options = listOf("强调色" to false, "活动色" to true),
                    selected = config.activityUseColorForText,
                    onSelect = { onUpdateConfig(config.copy(activityUseColorForText = it)) },
                )
            }
        }

        item {
            ConfigExpandableSection(title = "标签配置") {
                ChipSelectorRow(
                    label = "样式",
                    options = ChipDisplayMode.entries,
                    selected = config.tagDisplayMode,
                    display = { it.displayName() },
                    onSelect = { onUpdateConfig(config.copy(tagDisplayMode = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChipSelectorRow(
                    label = "布局",
                    options = GridLayoutMode.entries,
                    selected = config.tagLayoutMode,
                    display = { it.displayName() },
                    onSelect = { onUpdateConfig(config.copy(tagLayoutMode = it)) },
                )
                if (config.tagLayoutMode == GridLayoutMode.Vertical) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepperControl(
                        label = "每列行数",
                        value = config.tagColumnLines,
                        min = 1,
                        max = 10,
                        onValueChange = { onUpdateConfig(config.copy(tagColumnLines = it)) },
                    )
                }
                if (config.tagLayoutMode == GridLayoutMode.Horizontal) {
                    Spacer(modifier = Modifier.height(8.dp))
                    StepperControl(
                        label = "最多行数（0=无限）",
                        value = config.tagHorizontalLines,
                        min = 0,
                        max = 10,
                        infiniteAtMin = true,
                        onValueChange = { onUpdateConfig(config.copy(tagHorizontalLines = it)) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                ToggleControl(
                    label = "文字配色",
                    options = listOf("强调色" to false, "活动色" to true),
                    selected = config.tagUseColorForText,
                    onSelect = { onUpdateConfig(config.copy(tagUseColorForText = it)) },
                )
            }
        }

        item {
            ConfigExpandableSection(title = "其他") {
                ToggleControl(
                    label = "行为类型选择器",
                    options = listOf("隐藏" to false, "显示" to true),
                    selected = config.showBehaviorNature,
                    onSelect = { onUpdateConfig(config.copy(showBehaviorNature = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChipSelectorRow(
                    label = "路径动画模式",
                    options = PathDrawMode.entries,
                    selected = config.pathDrawMode,
                    display = { it.displayName() },
                    onSelect = { onUpdateConfig(config.copy(pathDrawMode = it)) },
                )
                Spacer(modifier = Modifier.height(8.dp))
                ChipSelectorRow(
                    label = "秒数策略",
                    options = SecondsStrategy.entries,
                    selected = config.secondsStrategy,
                    display = { it.displayName() },
                    onSelect = { onUpdateConfig(config.copy(secondsStrategy = it)) },
                )
            }
        }
    }
}

@Composable
private fun ConfigExpandableSection(
    title: String,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                    )
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun <T> ChipSelectorRow(
    label: String,
    options: List<T>,
    selected: T,
    display: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option ->
            Surface(
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(8.dp),
                color = if (option == selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = display(option),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (option == selected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (option == selected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun ToggleControl(
    label: String,
    options: List<Pair<String, Boolean>>,
    selected: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (text, value) ->
            Surface(
                onClick = { onSelect(value) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected == value) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected == value) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (selected == value) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun StepperControl(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    infiniteAtMin: Boolean = false,
    onValueChange: (Int) -> Unit,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            onClick = { if (value > min) onValueChange(value - 1) },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Text(
                text = "\u2212",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        Text(
            text = if (infiniteAtMin && value == min) "\u221E" else "$value",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
        Surface(
            onClick = {
                if (value < max) onValueChange(value + 1)
                else if (infiniteAtMin) onValueChange(min)
            },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

private fun ChipDisplayMode.displayName() = when (this) {
    ChipDisplayMode.None -> "无"
    ChipDisplayMode.Filled -> "填充"
    ChipDisplayMode.Underline -> "下划线"
    ChipDisplayMode.Capsules -> "胶囊"
    ChipDisplayMode.RoundedCorners -> "圆角"
    ChipDisplayMode.Squares -> "方块"
    ChipDisplayMode.SquareBorder -> "方框"
    ChipDisplayMode.HandDrawn -> "手绘"
    ChipDisplayMode.DashedLines -> "虚线"
}

private fun GridLayoutMode.displayName() = when (this) {
    GridLayoutMode.Horizontal -> "水平"
    GridLayoutMode.Vertical -> "垂直"
}

private fun PathDrawMode.displayName() = when (this) {
    PathDrawMode.StartToEnd -> "单向"
    PathDrawMode.BothSidesToMiddle -> "双向"
    PathDrawMode.Random -> "随机"
    PathDrawMode.None -> "无"
    PathDrawMode.WrigglingMaggot -> "蛆动"
}

private fun SecondsStrategy.displayName() = when (this) {
    SecondsStrategy.OPEN_TIME -> "打开时"
    SecondsStrategy.CONFIRM_TIME -> "确认时"
}
