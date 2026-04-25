package com.nltimer.feature.stats.ui

// Mark 逻辑 - 统计数据计算、图表数据准备、用户筛选交互、数据更新
// Mark 样式 - 统计页面布局、图表样式、数据可视化优化、动画效果
// Mark... - 性能调优、大数据量处理、导出功能、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsRoute() {
    // Mark 逻辑 - 路由入口函数，负责 ViewModel 初始化和统计数据加载
    StatsScreen()
}

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    // Mark 样式 - 统计页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Mark 样式 - 统计页图标样式，后续可替换为图表相关图标
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Mark 样式 - 统计页标题文字样式
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
