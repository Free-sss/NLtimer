package com.nltimer.app

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class SettingsNavigationScaffoldTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settingsRoute_showsGlobalBars_butThemeRouteHidesThem() {
        composeTestRule.onNodeWithText("设置").performClick()

        composeTestRule.onNodeWithContentDescription("打开侧边栏").assertExists()
        composeTestRule.onNodeWithText("主题配置").performClick()

        composeTestRule.onNodeWithContentDescription("打开侧边栏").assertDoesNotExist()
        composeTestRule.onNodeWithText("主题配置").assertExists()
        composeTestRule.onNodeWithContentDescription("返回").assertExists()
    }
}
