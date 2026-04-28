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

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun RouteSettingsPopup(
    currentRoute: String?,
    onDismiss: () -> Unit,
    onHomeLayoutChange: (HomeLayout) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val popupWidth = screenWidth * 0.45f
    val currentLayout = LocalTheme.current.homeLayout
    var showLayoutOptions by remember { mutableStateOf(false) }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = 0, y = 100), // Approximate offset from top app bar
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
                Text(
                    text = "页面设置: ${currentRoute ?: "未知"}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider()

                if (currentRoute == "home") {
                    PopupItem(
                        icon = Icons.Default.Dashboard,
                        label = if (showLayoutOptions) "返回设置" else "更改布局",
                        onClick = { showLayoutOptions = !showLayoutOptions }
                    )
                    
                    if (showLayoutOptions) {
                        HomeLayout.values().forEach { layout ->
                            val isSelected = currentLayout == layout
                            PopupItem(
                                icon = if (isSelected) Icons.Default.Search else Icons.Default.Dashboard, // Dummy icon for selection
                                label = when(layout) {
                                    HomeLayout.GRID -> "网格时间"
                                    HomeLayout.TIMELINE_REVERSE -> "时间轴(反)"
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
