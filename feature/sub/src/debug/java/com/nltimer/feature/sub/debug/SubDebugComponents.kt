package com.nltimer.feature.sub.debug

import com.nltimer.core.designsystem.debug.DebugComponentRegistry

/**
 * 副页调试组件注册器，集中管理调试模式下副页模块的所有可调试组件。
 * 通过 DebugComponentRegistry 将组件注册到统一调试入口，方便开发阶段验证。
 */
object SubDebugComponents {
    /**
     * 注册副页模块的所有调试组件到全局调试注册表。
     * 当前为空实现，后续在此方法内逐个添加子页面/子组件的调试入口。
     */
    fun registerAll() {
        // 待实现：逐一注册副页子组件的调试面板
    }
}
