package com.nltimer.feature.stats.ui

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

/**
 * 统计功能的路由入口。
 * 负责初始化 ViewModel 并触发统计数据加载流程。
 * 后续可在此处注入依赖（如 StatsViewModel）并传递给 StatsScreen。
 */
@Composable
fun StatsRoute() {
    StatsScreen()
}

/**
 * 统计页面的主体内容。
 * 当前为占位实现，居中显示柱状图图标和"统计"标题。
 * 后续将替换为完整的图表组件、筛选控件和数据展示区域。
 *
 * @param modifier 应用于根布局的 Modifier，用于外部控制布局位置和内边距
 */
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    // 使用 Scaffold 提供标准页面骨架，padding 参数确保内容避开系统栏
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            // 内容垂直水平居中，符合空状态占位页面的视觉规范
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 柱状图图标，64dp 尺寸确保在页面中具有足够的视觉权重
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // 标题文字，使用 headlineLarge 样式保持与全局排版风格一致
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
