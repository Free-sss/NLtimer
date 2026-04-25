package com.nltimer.feature.home.ui

// Mark 逻辑 - 主页数据加载、计时器状态显示、用户交互处理、导航跳转
// Mark 样式 - 主页布局优化、视觉层次调整、响应式适配、动画效果
// Mark... - 性能优化、测试用例、错误处理、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeRoute() {
    // Mark 逻辑 - 路由入口函数，负责 ViewModel 初始化和状态传递
    HomeScreen()
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    // Mark 样式 - 主页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Mark 样式 - 主页图标样式，后续可替换为自定义图标
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Mark 样式 - 主页标题文字样式
            Text(
                text = "主页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
