package com.nltimer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用 Application 类
 * 使用 Hilt 组件注入，在启动时通过反射尝试初始化 debug 模块组件
 */
@HiltAndroidApp
class NLtimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 尝试通过反射加载 DebugInitializer，debug 构建存在则执行初始化
        initializeDebugIfPresent()
    }

    /**
     * 通过反射调用 DebugInitializer.init()
     * debug 构建中包含 DebugInitializer 类，release 构建中该类不存在，静默忽略异常
     */
    private fun initializeDebugIfPresent() {
        try {
            // 反射查找 debug 模块的初始化类并调用其静态 init 方法
            Class.forName("com.nltimer.app.DebugInitializer")
                .getMethod("init")
                .invoke(null)
        } catch (_: Exception) {
            // release 构建无 DebugInitializer，忽略 ClassNotFoundException
        }
    }
}
