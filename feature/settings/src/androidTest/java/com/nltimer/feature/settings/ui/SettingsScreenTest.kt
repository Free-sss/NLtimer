package com.nltimer.feature.settings.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.nltimer.core.designsystem.theme.NLtimerTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_doesNotRenderDuplicateLargeTitle() {
        composeTestRule.setContent {
            NLtimerTheme {
                SettingsScreen()
            }
        }

        composeTestRule.onAllNodesWithText("设置").assertCountEquals(0)
        composeTestRule.onNodeWithText("主题配置").assertHasClickAction()
        composeTestRule.onNodeWithText("弹窗配置").assertHasClickAction()
    }
}
