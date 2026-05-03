package com.nltimer.app.component

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.tooling.preview.Preview
import com.nltimer.core.designsystem.theme.HomeLayout
import com.nltimer.core.designsystem.theme.LocalTheme
import com.nltimer.core.designsystem.theme.NLtimerTheme

/**
 * RouteSettingsPopup 调试预览入口
 * 为开发者提供当前弹窗在 home 路由下的预览快照
 */
@Preview(showBackground = true)
@Composable
fun RouteSettingsPopupPreview() {
    NLtimerTheme {
        RouteSettingsPopup(
            currentRoute = "home",
            onDismiss = {},
            onHomeLayoutChange = {}
        )
    }
}

/**
 * 页面设置弹出菜单 Composable
 * 根据当前路由显示对应的配置项：home 路由下可切换布局，其他路由显示通用快捷操作
 *
 * @param currentRoute 当前路由名称，用于判定显示哪些设置项
 * @param onDismiss 关闭弹窗的回调
 * @param onHomeLayoutChange 主页布局变更回调
 * @param modifier Modifier 修饰符
 */
@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun RouteSettingsPopup(
    currentRoute: String?,
    onDismiss: () -> Unit,
    onHomeLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier
) {
    // 根据屏幕宽度计算弹窗宽度，不超过屏幕的一半
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val popupWidth = screenWidth * 0.45f
    // 获取当前主页布局类型
    val currentLayout = LocalTheme.current.homeLayout
    // 控制布局选项的展开/折叠状态
    var showLayoutOptions by remember { mutableStateOf(false) }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = 100), // 相对顶栏的偏移量，使弹窗出现在顶栏下方
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = modifier
                .width(popupWidth)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // 弹窗标题，显示当前页面名称
                Text(
                    text = "页面设置: ${currentRoute ?: "未知"}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider()

                // home 路由专属：布局切换功能
                if (currentRoute == "home") {
                    PopupItem(
                        icon = Icons.Default.Dashboard,
                        label = if (showLayoutOptions) "返回设置" else "更改布局",
                        onClick = { showLayoutOptions = !showLayoutOptions }
                    )
                    
                    // 展开布局选项子菜单，遍历所有布局枚举供用户选择
                    if (showLayoutOptions) {
                        HomeLayout.values().forEach { layout ->
                            val isSelected = currentLayout == layout
                            PopupItem(
                                icon = if (isSelected) Icons.Default.Search else Icons.Default.Dashboard,
                                label = when(layout) {
                                    HomeLayout.GRID -> "网格时间"
                                    HomeLayout.TIMELINE_REVERSE -> "时间轴(反)"
                                    HomeLayout.LOG -> "行为日志"
                                },
                                onClick = {
                                    onHomeLayoutChange(layout)
                                    onDismiss()
                                },
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 未展开布局选项时，显示通用快捷操作列表
                if (!showLayoutOptions) {
                    PopupItem(
                        icon = Icons.Default.Search,
                        label = "搜索",
                        onClick = { /* TODO */ onDismiss() }
                    )
                    PopupItem(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = "活动管理",
                        onClick = { /* TODO */ onDismiss() }
                    )
                    PopupItem(
                        icon = Icons.AutoMirrored.Filled.Label,
                        label = "标签管理",
                        onClick = { /* TODO */ onDismiss() }
                    )
                    PopupItem(
                        icon = Icons.AutoMirrored.Filled.Accessible,
                        label = "导出今日记录",
                        onClick = { /* TODO */ onDismiss() }
                    )
                }
            }
        }
    }
}

/**
 * 弹窗内单行菜单项 Composable
 * 显示图标和文本标签，支持点击事件和自定义着色
 *
 * @param icon 菜单项图标
 * @param label 菜单项文本标签
 * @param onClick 点击回调
 * @param tint 图标和文本的颜色
 */
@Composable
private fun PopupItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.padding(end = 12.dp),
            tint = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}
