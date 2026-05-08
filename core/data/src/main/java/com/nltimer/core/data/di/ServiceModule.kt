package com.nltimer.core.data.di

import com.nltimer.core.data.util.ClockService
import com.nltimer.core.data.util.SystemClockService
import com.nltimer.core.data.util.TimeSnapService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideClockService(): ClockService = SystemClockService()

    @Provides
    @Singleton
    fun provideTimeSnapService(): TimeSnapService = TimeSnapService()
}
