package com.nltimer.feature.sub.ui

// Mark 逻辑 - 副页功能实现、数据绑定、业务逻辑处理、用户交互
// Mark 样式 - 副页界面优化、组件对齐、视觉一致性、动画效果
// Mark... - 功能完善、测试覆盖、边界情况处理、无障碍支持等开发事项

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubRoute() {
    // Mark 逻辑 - 路由入口函数，负责 ViewModel 初始化和状态传递
    SubScreen()
}

@Composable
fun SubScreen(modifier: Modifier = Modifier) {
    // Mark 样式 - 副页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Mark 样式 - 副页图标样式，后续可替换为业务相关图标
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // Mark 样式 - 副页标题文字样式
            Text(
                text = "副页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
