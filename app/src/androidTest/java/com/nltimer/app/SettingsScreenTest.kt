package com.nltimer.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsScreen_doesNotRenderDuplicateLargeTitle() {
        composeTestRule.onNodeWithText("设置").performClick()

        composeTestRule.onAllNodesWithText("设置").assertCountEquals(1)
        composeTestRule.onNodeWithText("主题配置").assertHasClickAction()
        composeTestRule.onNodeWithText("弹窗配置").assertHasClickAction()
    }
}
