package com.nltimer.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NLtimerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeDebugIfPresent()
    }

    private fun initializeDebugIfPresent() {
        try {
            Class.forName("com.nltimer.app.DebugInitializer")
                .getMethod("init")
                .invoke(null)
        } catch (_: ClassNotFoundException) {
        }
    }
}
