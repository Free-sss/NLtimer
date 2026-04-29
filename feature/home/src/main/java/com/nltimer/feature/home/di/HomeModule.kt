package com.nltimer.feature.home.di

import com.nltimer.feature.home.match.KeywordMatchStrategy
import com.nltimer.feature.home.match.MatchStrategy
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for the home feature.
 * Provides singleton-scoped dependencies for the home module.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    // 将 KeywordMatchStrategy 绑定为 MatchStrategy 接口的单例实现
    @Binds
    @Singleton
    abstract fun bindMatchStrategy(impl: KeywordMatchStrategy): MatchStrategy
}
