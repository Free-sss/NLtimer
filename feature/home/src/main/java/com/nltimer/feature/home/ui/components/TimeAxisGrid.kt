package com.nltimer.feature.home.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.toDisplayString
import com.nltimer.feature.home.model.GridRowUiState

/**
 * 网格时间轴 Composable — 纵向滚动的网格布局。
 * 支持布局切换菜单，并根据当前小时自动滚动到对应位置。
 *
 * @param modifier 修饰符
 * @param rows 所有网格行数据
 * @param onEmptyCellClick 点击空单元格回调
 * @param onLayoutChange 切换布局模式回调
 * @param currentHour 当前选中的小时
 */
@Composable
fun TimeAxisGrid(
    modifier: Modifier = Modifier,
    rows: List<GridRowUiState>,
    onEmptyCellClick: () -> Unit,
    onLayoutChange: (HomeLayout) -> Unit,
    currentHour: Int = 0,
) {
    val listState = rememberLazyListState()
    var showLayoutMenu by remember { mutableStateOf(false) }

    // 当 currentHour 变化时，自动滚动到对应时间行
    LaunchedEffect(currentHour) {
        val targetIndex = rows.indexOfFirst { it.startTime.hour >= currentHour }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(start = 10.dp, end = 16.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 标题行：显示"网格时间"且可点开布局切换菜单
        item {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showLayoutMenu = true }
                ) {
                    Text(
                        text = "网格时间",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showLayoutMenu,
                    onDismissRequest = { showLayoutMenu = false }
                ) {
                    HomeLayout.values().forEach { layout ->
                        DropdownMenuItem(
                            text = { Text(layout.toDisplayString()) },
                            onClick = {
                                onLayoutChange(layout)
                                showLayoutMenu = false
                            }
                        )
                    }
                }
            }
        }

        items(items = rows, key = { it.rowId }) { row ->
            GridRow(
                row = row,
                onEmptyCellClick = onEmptyCellClick,
            )
        }
    }
}
