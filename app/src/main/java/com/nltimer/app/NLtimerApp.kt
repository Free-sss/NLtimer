package com.nltimer.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController

/**
 * 应用根 Composable 入口
 * 创建导航控制器并传递给 Scaffold 以管理全局导航状态
 */
@Composable
fun NLtimerApp() {
    // 创建导航控制器实例，贯穿整个应用生命周期
    val navController = rememberNavController()
    NLtimerScaffold(navController = navController)
}
