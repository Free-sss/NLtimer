package com.nltimer.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.data.model.HomeLayoutConfig
import com.nltimer.core.data.model.GridLayoutStyle
import com.nltimer.core.data.model.LogLayoutStyle
import com.nltimer.core.designsystem.component.ExpandableCard
import com.nltimer.core.designsystem.component.atom.SelectableOptionChip

@Composable
fun HomeLayoutConfigRoute(
    viewModel: DialogConfigViewModel = hiltViewModel(),
) {
    val config by viewModel.homeLayoutConfig.collectAsStateWithLifecycle()
    HomeLayoutConfigScreen(
        config = config,
        onUpdateConfig = viewModel::updateHomeLayoutConfig,
    )
}

@Composable
fun HomeLayoutConfigScreen(
    config: HomeLayoutConfig,
    onUpdateConfig: (HomeLayoutConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedGrid by remember { mutableStateOf(false) }
    var expandedLog by remember { mutableStateOf(false) }
    var expandedOther by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ExpandableCard(
                    title = "网格布局",
                    expanded = expandedGrid,
                    onToggle = { expandedGrid = !expandedGrid },
                ) {
                    ConfigStepper(
                        label = "列数",
                        value = config.grid.columns,
                        min = 2, max = 6,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(columns = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "最小行高",
                        value = config.grid.minRowHeight,
                        suffix = "dp",
                        min = 60, max = 200,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(minRowHeight = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "最大单元高",
                        value = config.grid.maxCellHeight,
                        suffix = "dp",
                        min = 80, max = 300,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(maxCellHeight = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "列间距",
                        value = config.grid.columnSpacing,
                        suffix = "dp",
                        min = 0, max = 20,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(columnSpacing = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "内边距",
                        value = config.grid.cellPadding,
                        suffix = "dp",
                        min = 0, max = 16,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(cellPadding = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "图标大小",
                        value = config.grid.iconSize,
                        suffix = "dp",
                        min = 8, max = 24,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(iconSize = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "标签缩放",
                        value = (config.grid.tagScale * 10).toInt(),
                        suffix = "/10",
                        min = 5, max = 10,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(tagScale = it / 10f))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "标签间距",
                        value = config.grid.tagSpacing,
                        suffix = "dp",
                        min = 0, max = 8,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(tagSpacing = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "激活背景透明度",
                        value = (config.grid.activeBgAlpha * 100).toInt(),
                        suffix = "%",
                        min = 10, max = 80, step = 5,
                        onValueChange = { onUpdateConfig(config.copy(grid = config.grid.copy(activeBgAlpha = it / 100f))) },
                    )
                }
            }

            item {
                ExpandableCard(
                    title = "日志布局",
                    expanded = expandedLog,
                    onToggle = { expandedLog = !expandedLog },
                ) {
                    ConfigStepper(
                        label = "卡片内边距",
                        value = config.log.cardPadding,
                        suffix = "dp",
                        min = 4, max = 24,
                        onValueChange = { onUpdateConfig(config.copy(log = config.log.copy(cardPadding = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "图标大小",
                        value = config.log.iconSize,
                        suffix = "dp",
                        min = 8, max = 32,
                        onValueChange = { onUpdateConfig(config.copy(log = config.log.copy(iconSize = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "图标间距",
                        value = config.log.iconSpacing,
                        suffix = "dp",
                        min = 2, max = 16,
                        onValueChange = { onUpdateConfig(config.copy(log = config.log.copy(iconSpacing = it))) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConfigStepper(
                        label = "标签行间距",
                        value = config.log.tagRowSpacing,
                        suffix = "dp",
                        min = 2, max = 16,
                        onValueChange = { onUpdateConfig(config.copy(log = config.log.copy(tagRowSpacing = it))) },
                    )
                }
            }

            item {
                ExpandableCard(
                    title = "其他布局",
                    expanded = expandedOther,
                    onToggle = { expandedOther = !expandedOther },
                ) {
                    Text(
                        text = "时间轴和当前时刻布局暂无可配置参数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(onClick = { onUpdateConfig(HomeLayoutConfig()) }) {
                        Text("恢复默认")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    step: Int = 1,
    suffix: String = "",
    onValueChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f),
        )
        Surface(
            onClick = { if (value > min) onValueChange(value - step) },
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
            text = "$value$suffix",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.weight(0.2f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Surface(
            onClick = { if (value < max) onValueChange(value + step) },
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
