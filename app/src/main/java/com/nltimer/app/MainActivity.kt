package com.nltimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nltimer.core.data.SettingsPrefs
import com.nltimer.core.designsystem.theme.NLtimerTheme
import com.nltimer.core.designsystem.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsPrefs: SettingsPrefs

    private val themeReady = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !themeReady.get() }
        enableEdgeToEdge()

        setContent {
            val theme by settingsPrefs.getThemeFlow()
                .collectAsStateWithLifecycle(initialValue = null)

            LaunchedEffect(theme) {
                if (theme != null) themeReady.set(true)
            }

            if (theme != null) {
                NLtimerTheme(theme = theme!!) {
                    NLtimerApp()
                }
            }
        }
    }
}
