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

/**
 * 副页路由入口，负责初始化副页所需状态并委托给 SubScreen 进行界面渲染。
 * 作为导航图的目标节点，接收来自主页的导航参数并管理 ViewModel 生命周期。
 */
@Composable
fun SubRoute() {
    // 路由入口函数，负责 ViewModel 初始化和状态传递
    SubScreen()
}

/**
 * 副页主屏幕组件，展示应用副功能的中心内容。
 * 当前为占位布局，居中显示副页图标与标题文本，后续将接入具体业务模块。
 *
 * @param modifier 应用于根容器的 Modifier，用于外部调整布局位置和内边距
 */
@Composable
fun SubScreen(modifier: Modifier = Modifier) {
    // 副页内容布局，居中显示图标和标题
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // 使用 Material 默认图标作为占位，待业务确定后替换为具体功能图标
            Icon(
                imageVector = Icons.Default.Apps,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            // 采用 headlineLarge 样式突出页面主题，上边距与图标保持视觉间距
            Text(
                text = "副页",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
