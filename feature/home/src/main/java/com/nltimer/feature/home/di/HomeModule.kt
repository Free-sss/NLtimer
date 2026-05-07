package com.nltimer.feature.home.di

import com.nltimer.core.data.util.TimeSnapService
import com.nltimer.feature.home.match.KeywordMatchStrategy
import com.nltimer.feature.home.match.MatchStrategy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindMatchStrategy(impl: KeywordMatchStrategy): MatchStrategy

    companion object {
        @Provides
        @Singleton
        fun provideTimeSnapService(): TimeSnapService = TimeSnapService()
    }
}
