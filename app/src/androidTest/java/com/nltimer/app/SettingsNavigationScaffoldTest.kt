package com.nltimer.app

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SettingsNavigationScaffoldTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsRoute_showsGlobalBars_butThemeRouteHidesThem() {
        composeTestRule.onNodeWithText("设置").performClick()

        composeTestRule.onAllNodesWithContentDescription("打开侧边栏").assertCountEquals(1)
        composeTestRule.onNodeWithText("主题配置").performClick()

        composeTestRule.onAllNodesWithContentDescription("打开侧边栏").assertCountEquals(0)
        composeTestRule.onAllNodesWithText("主题配置").assertCountEquals(1)
        composeTestRule.onAllNodesWithContentDescription("返回").assertCountEquals(1)
    }
}
