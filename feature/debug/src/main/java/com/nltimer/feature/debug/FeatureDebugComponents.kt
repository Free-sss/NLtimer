package com.nltimer.feature.debug

import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry
import com.nltimer.feature.debug.ui.AddActivityDialogPreview
import com.nltimer.feature.debug.ui.DualTimePickerDebugPreview
import com.nltimer.feature.debug.ui.TimeAdjustmentDebugPreview

/**
 * feature/debug 模块的调试组件注册器
 * 将本模块内待调试的 Composable 组件注册到 [DebugComponentRegistry]，
 * 使它们出现在调试页面的组件选择列表中
 */
object FeatureDebugComponents {
    fun registerAll() {
        DebugComponentRegistry.register(
            DebugComponent(
                id = "DualTimePicker",
                name = "双列时间选择器",
                group = "Pickers",
                description = "左右双列日期+时分滚轮选择器",
            ) {
                DualTimePickerDebugPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "TimeAdjustment",
                name = "时间步进调节器",
                group = "Inputs",
                description = "水平步进式时间增减按钮组",
            ) {
                TimeAdjustmentDebugPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "AddActivityDialog",
                name = "增加活动弹窗",
                group = "Dialogs",
                description = "全屏活动创建对话框，含图标/名称/备注/标签等表单",
            ) {
                AddActivityDialogPreview()
            }
        )
    }
}
