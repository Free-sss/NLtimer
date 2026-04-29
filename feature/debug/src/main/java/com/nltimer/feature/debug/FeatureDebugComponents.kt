package com.nltimer.feature.debug

import com.nltimer.core.designsystem.debug.DebugComponent
import com.nltimer.core.designsystem.debug.DebugComponentRegistry
import com.nltimer.feature.debug.ui.preview.AddActivityPreview
import com.nltimer.feature.debug.ui.preview.AddTagPreview
import com.nltimer.feature.debug.ui.preview.DualTimePickerDebugPreview
import com.nltimer.feature.debug.ui.preview.EditActivityPreview
import com.nltimer.feature.debug.ui.preview.EditTagPreview
import com.nltimer.feature.debug.ui.preview.TimeAdjustmentDebugPreview

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
                id = "AddActivity",
                name = "新增活动",
                group = "Forms",
                description = "新增活动表单，含图标/名称/备注/标签",
            ) {
                AddActivityPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "EditActivity",
                name = "编辑活动",
                group = "Forms",
                description = "编辑活动表单，预填模拟数据",
            ) {
                EditActivityPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "AddTag",
                name = "新增标签",
                group = "Forms",
                description = "新增标签表单，含图标/名称/分类",
            ) {
                AddTagPreview()
            }
        )
        DebugComponentRegistry.register(
            DebugComponent(
                id = "EditTag",
                name = "编辑标签",
                group = "Forms",
                description = "编辑标签表单，预填模拟数据",
            ) {
                EditTagPreview()
            }
        )
    }
}
