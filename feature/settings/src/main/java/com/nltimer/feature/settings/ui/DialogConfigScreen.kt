package com.nltimer.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.nltimer.core.designsystem.theme.ChipDisplayMode
import com.nltimer.core.designsystem.theme.GridLayoutMode
import com.nltimer.core.designsystem.theme.PathDrawMode
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogConfigScreen(
    config: DialogGridConfig,
    onUpdateConfig: (DialogGridConfig) -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.background,
    modifier: Modifier = Modifier,
) {
    SettingsSubpageScaffold { padding ->
        DialogConfigContent(
            config = config,
            onUpdateConfig = onUpdateConfig,
            contentPadding = padding,
            containerColor = containerColor,
            modifier = modifier,
        )
    }
}

@Composable
private fun DialogConfigContent(
    config: DialogGridConfig,
    onUpdateConfig: (DialogGridConfig) -> Unit,
    contentPadding: PaddingValues,
    containerColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(containerColor)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
            GridConfigBlock(
                label = "活动",
                displayMode = config.activityDisplayMode,
                layoutMode = config.activityLayoutMode,
                columnLines = config.activityColumnLines,
                horizontalLines = config.activityHorizontalLines,
                useColorForText = config.activityUseColorForText,
                onDisplayModeChange = { onUpdateConfig(config.copy(activityDisplayMode = it)) },
                onLayoutModeChange = { onUpdateConfig(config.copy(activityLayoutMode = it)) },
                onColumnLinesChange = { onUpdateConfig(config.copy(activityColumnLines = it)) },
                onHorizontalLinesChange = { onUpdateConfig(config.copy(activityHorizontalLines = it)) },
                onUseColorForTextChange = { onUpdateConfig(config.copy(activityUseColorForText = it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            GridConfigBlock(
                label = "标签",
                displayMode = config.tagDisplayMode,
                layoutMode = config.tagLayoutMode,
                columnLines = config.tagColumnLines,
                horizontalLines = config.tagHorizontalLines,
                useColorForText = config.tagUseColorForText,
                onDisplayModeChange = { onUpdateConfig(config.copy(tagDisplayMode = it)) },
                onLayoutModeChange = { onUpdateConfig(config.copy(tagLayoutMode = it)) },
                onColumnLinesChange = { onUpdateConfig(config.copy(tagColumnLines = it)) },
                onHorizontalLinesChange = { onUpdateConfig(config.copy(tagHorizontalLines = it)) },
                onUseColorForTextChange = { onUpdateConfig(config.copy(tagUseColorForText = it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "其他",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ToggleControl(
                        label = "显示行为类型选择器",
                        checked = config.showBehaviorNature,
                        onCheckedChange = { onUpdateConfig(config.copy(showBehaviorNature = it)) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PathDrawModeSelector(
                        currentMode = config.pathDrawMode,
                        onModeChange = { onUpdateConfig(config.copy(pathDrawMode = it)) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
internal fun GridConfigBlock(
    label: String,
    displayMode: ChipDisplayMode,
    layoutMode: GridLayoutMode,
    columnLines: Int,
    horizontalLines: Int,
    useColorForText: Boolean,
    onDisplayModeChange: (ChipDisplayMode) -> Unit,
    onLayoutModeChange: (GridLayoutMode) -> Unit,
    onColumnLinesChange: (Int) -> Unit,
    onHorizontalLinesChange: (Int) -> Unit,
    onUseColorForTextChange: (Boolean) -> Unit,
) {
    var modeExpanded by remember { mutableStateOf(false) }
    var layoutExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$label 配置",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "样式",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Surface(
                            onClick = { modeExpanded = true },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Text(
                                text = displayMode.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = modeExpanded,
                            onDismissRequest = { modeExpanded = false },
                        ) {
                            ChipDisplayMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.name,
                                            fontWeight = if (mode == displayMode) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        onDisplayModeChange(mode)
                                        modeExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "布局",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Surface(
                            onClick = { layoutExpanded = true },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Text(
                                text = layoutMode.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = layoutExpanded,
                            onDismissRequest = { layoutExpanded = false },
                        ) {
                            GridLayoutMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = mode.name,
                                            fontWeight = if (mode == layoutMode) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    },
                                    onClick = {
                                        onLayoutModeChange(mode)
                                        layoutExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (layoutMode == GridLayoutMode.Vertical) {
                StepperControl(
                    label = "每列行数",
                    value = columnLines,
                    min = 1,
                    max = 10,
                    onValueChange = onColumnLinesChange,
                )
            }

            if (layoutMode == GridLayoutMode.Horizontal) {
                StepperControl(
                    label = "最多行数（0=无限）",
                    value = horizontalLines,
                    min = 0,
                    max = 10,
                    infiniteAtMin = true,
                    onValueChange = onHorizontalLinesChange,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            ToggleControl(
                label = "文字使用活动色",
                checked = useColorForText,
                onCheckedChange = onUseColorForTextChange,
            )
        }
    }
}

@Composable
private fun PathDrawModeSelector(
    currentMode: PathDrawMode,
    onModeChange: (PathDrawMode) -> Unit,
) {
    Text(
        text = "路径动画模式",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathDrawMode.entries.forEach { mode ->
            Surface(
                onClick = { onModeChange(mode) },
                shape = RoundedCornerShape(6.dp),
                color = if (mode == currentMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Text(
                    text = when (mode) {
                        PathDrawMode.StartToEnd -> "单向"
                        PathDrawMode.BothSidesToMiddle -> "双向"
                        PathDrawMode.Random -> "随机"
                        PathDrawMode.None -> "无"
                        PathDrawMode.WrigglingMaggot -> "蛆动"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (mode == currentMode) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (mode == currentMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
internal fun ToggleControl(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
            onClick = { onCheckedChange(false) },
            shape = RoundedCornerShape(6.dp),
            color = if (!checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "强调色",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (!checked) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (!checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Surface(
            onClick = { onCheckedChange(true) },
            shape = RoundedCornerShape(6.dp),
            color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "活动色",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
internal fun StepperControl(
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
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}
