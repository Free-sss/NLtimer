package com.nltimer.app.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

/**
 * 应用顶部栏 Composable
 * 居中显示标题，左侧菜单按钮打开侧边栏，右侧设置按钮弹出页面配置
 *
 * @param onMenuClick 点击菜单按钮的回调
 * @param onSettingClick 点击设置按钮的回调
 * @param modifier Modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    onSettingClick: () -> Unit,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "打开侧边栏",
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "配置项",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCollapsedTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    onSettingClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    lineHeight = 32.sp,
                ),
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "打开侧边栏",
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "配置项",
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        modifier = modifier,
    )
}
