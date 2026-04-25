package com.nltimer.app.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

// Mark 逻辑 - 侧边栏菜单项点击事件处理、状态管理、导航路由跳转
// Mark 样式 - 侧边栏宽度优化为 50% 屏幕宽度、间距调整、动画效果完善
// Mark... - 无障碍支持、主题适配、多语言支持等其他开发事项

// Mark 逻辑 - 菜单项数据模型定义，包含路由、图标、选中状态
private data class DrawerMenuItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selected: Boolean = false,
)

// Mark 样式 - 菜单项列表配置，后续根据实际功能调整图标和路由
private val drawerMenuItems = listOf(
    DrawerMenuItem("home", "选项一", Icons.AutoMirrored.Filled.List),
    DrawerMenuItem("settings", "选项二", Icons.Default.Brightness5),
)

@Composable
fun AppDrawer(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Mark 样式 - 侧边栏宽度响应式计算，适配不同屏幕尺寸
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val maxDrawerWidth = (screenWidthDp * 0.5f).coerceAtLeast(280.dp)

    // Mark 逻辑 - 当前选中菜单项状态管理，用于高亮显示
    var selectedItemIndex by remember { mutableStateOf<Int?>(null) }

    ModalDrawerSheet(
        // Mark 样式 - 宽度限制：最小 280dp 保证可读，最大 50% 屏幕宽度
        modifier = modifier.widthIn(
            min = 280.dp,
            max = maxDrawerWidth,
        ),
    ) {
        // Mark 逻辑 - 侧边栏标题区域，应用名称和品牌展示
        Text(
            text = "NLtimer",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Mark 逻辑 - 菜单项列表渲染，点击后导航到对应页面
        drawerMenuItems.forEachIndexed { index, item ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = { Text(item.label) },
                selected = selectedItemIndex == index,
                onClick = {
                    selectedItemIndex = index
                    onClose()
                },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }

        // Mark... - 更多菜单项、分隔线、底部版本信息区域等后续扩展
    }
}
