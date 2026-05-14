package com.nltimer.core.tools.di

import com.nltimer.core.tools.ToolDefinition
import com.nltimer.core.tools.ToolRegistry
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

/**
 * 工具层 Hilt 模块
 *
 * 通过 Multibinding 收集所有 [ToolDefinition]，由 Hilt 自动构造并注入 [ToolRegistry]。
 *
 * 后续新增工具时，在该工具自己的模块（或所属 feature 模块）中：
 *
 * ```
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class QueryActiveSessionToolModule {
 *     @Binds
 *     @IntoSet
 *     abstract fun bindQueryActiveSessionTool(impl: QueryActiveSessionTool): ToolDefinition
 * }
 * ```
 *
 * [Multibinds] 保证即使没有任何工具被注册，注入点也能拿到空 Set 而不会报错，
 * 这样模块在工具尚未实现时仍可正常编译运行。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ToolsModule {

    @Multibinds
    abstract fun toolDefinitions(): Set<ToolDefinition>
}
