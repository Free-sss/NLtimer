package com.nltimer.core.data.di

import android.content.Context
import androidx.room.Room
import com.nltimer.core.data.database.NLtimerDatabase
import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.data.repository.impl.ActivityRepositoryImpl
import com.nltimer.core.data.repository.impl.BehaviorRepositoryImpl
import com.nltimer.core.data.repository.impl.CategoryRepositoryImpl
import com.nltimer.core.data.repository.impl.TagRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    abstract fun bindBehaviorRepository(impl: BehaviorRepositoryImpl): BehaviorRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): NLtimerDatabase =
            Room.databaseBuilder(
                context,
                NLtimerDatabase::class.java,
                "nltimer-database",
            )
                .fallbackToDestructiveMigration(false)
                .build()

        @Provides
        fun provideActivityDao(database: NLtimerDatabase): ActivityDao =
            database.activityDao()

        @Provides
        fun provideTagDao(database: NLtimerDatabase): TagDao =
            database.tagDao()

        @Provides
        fun provideBehaviorDao(database: NLtimerDatabase): BehaviorDao =
            database.behaviorDao()
    }
}
