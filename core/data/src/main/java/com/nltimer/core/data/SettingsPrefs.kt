package com.nltimer.core.data

import com.nltimer.core.data.model.DialogGridConfig
import com.nltimer.core.designsystem.theme.Theme
import kotlinx.coroutines.flow.Flow

/**
 * SettingsPrefs 用户偏好设置接口
 * 提供主题配置、标签分类和弹窗配置的持久化读写能力
 */
interface SettingsPrefs {
    /** 以 Flow 形式监听当前主题设置 */
    fun getThemeFlow(): Flow<Theme>
    /** 更新主题设置并持久化 */
    suspend fun updateTheme(theme: Theme)

    /** 获取已保存的标签分类集合 */
    fun getSavedTagCategories(): Flow<Set<String>>
    /** 保存标签分类列表到持久化存储 */
    suspend fun saveTagCategories(categories: Set<String>)

    /** 以 Flow 形式监听弹窗配置 */
    fun getDialogConfigFlow(): Flow<DialogGridConfig>
    /** 更新弹窗配置并持久化 */
    suspend fun updateDialogConfig(config: DialogGridConfig)
}
