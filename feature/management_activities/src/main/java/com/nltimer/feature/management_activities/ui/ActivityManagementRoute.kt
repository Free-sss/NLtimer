package com.nltimer.feature.management_activities.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.nltimer.feature.management_activities.viewmodel.ActivityManagementViewModel

/**
 * 活动管理页面的路由入口
 *
 * 通过 Hilt 自动注入 ViewModel，并委托给 ActivityManagementScreen 渲染。
 *
 * @param viewModel 活动管理的 ViewModel，默认由 Hilt 注入
 */
@Composable
fun ActivityManagementRoute(
    viewModel: ActivityManagementViewModel = hiltViewModel(),
) {
    // 委托给实际的 Screen 组合函数进行页面渲染
    ActivityManagementScreen(viewModel = viewModel)
}
