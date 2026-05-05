package com.nltimer.app

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用 Application 类
 * 使用 Hilt 组件注入，启动时初始化 debug 模块组件（debug 构建中生效）。
 */
@HiltAndroidApp
class NLtimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDebugIfPresent()
    }

    /**
     * 通过反射调用 DebugInitializer.init()
     * debug 构建中 DebugInitializer 类存在，release 中不存在则静默跳过。
     * Class.forName 的结果由 JVM 内部缓存，仅首次调用有微小开销。
     */
    private fun initializeDebugIfPresent() {
        try {
            Class.forName("com.nltimer.app.DebugInitializer")
                .getMethod("init")
                .invoke(null)
        } catch (_: ClassNotFoundException) {
            // release 构建无 DebugInitializer 类，预期异常
        } catch (e: Exception) {
            Log.w("NLtimerApplication", "DebugInitializer init failed", e)
        }
    }
}
