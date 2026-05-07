package com.nltimer.core.tools.match

import com.nltimer.core.tools.ToolDefinition
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/**
 * Match 类工具 Hilt 多绑定模块
 *
 * 通过 @IntoSet 把 [MatchActivitiesAndTagsTool] 贡献到全局 Set<ToolDefinition>，
 * 由 [com.nltimer.core.tools.ToolRegistry] 构造函数自动消费。
 *
 * 后续若新增其他匹配类工具（如 MatchBehaviorsTool），在此模块追加 @Binds @IntoSet 方法即可。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MatchToolsModule {

    @Binds
    @IntoSet
    abstract fun bindMatchActivitiesAndTagsTool(impl: MatchActivitiesAndTagsTool): ToolDefinition
}
