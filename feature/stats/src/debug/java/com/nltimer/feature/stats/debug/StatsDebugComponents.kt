package com.nltimer.feature.stats.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

/**
 * 统计模块的调试组件注册中心。
 * 集中管理统计功能相关的调试入口（如伪造数据预览、图表调试面板等），
 * 仅在 debug 构建变体下编译，避免影响 release 包的体积与安全性。
 */
object StatsDebugComponents {
    /**
     * 向全局 DebugComponentRegistry 注册所有统计调试组件。
     * 当前为占位实现，待统计图表模块完成后在此注册对应的调试面板。
     */
    fun registerAll() { }
}
