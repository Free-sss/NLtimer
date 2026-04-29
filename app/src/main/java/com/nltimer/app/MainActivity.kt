package com.nltimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.designsystem.theme.NLtimerTheme
import com.nltimer.core.designsystem.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 应用主入口 Activity
 * 使用 Hilt 注入主题偏好设置，在 Compose 中读取主题并渲染应用界面
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsPrefs: SettingsPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // 从 DataStore 中收集主题配置流，获取当前主题状态
            val theme by settingsPrefs.getThemeFlow()
                .collectAsStateWithLifecycle(initialValue = Theme())

            // 使用动态主题包裹应用根组件
            NLtimerTheme(theme = theme) {
                NLtimerApp()
            }
        }
    }
}
