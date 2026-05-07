package com.nltimer.core.data.di

import android.content.Context
import androidx.room.Room
import com.nltimer.core.data.database.NLtimerDatabase
import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.ActivityGroupDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.repository.ActivityManagementRepository
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.data.repository.CategoryRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.data.repository.impl.ActivityManagementRepositoryImpl
import com.nltimer.core.data.repository.impl.ActivityRepositoryImpl
import com.nltimer.core.data.repository.impl.BehaviorRepositoryImpl
import com.nltimer.core.data.repository.impl.CategoryRepositoryImpl
import com.nltimer.core.data.repository.impl.TagRepositoryImpl
import com.nltimer.core.data.util.ClockService
import com.nltimer.core.data.util.SystemClockService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataModule Hilt 数据层依赖注入模块
 * 绑定 Repository 接口与实现，并提供 Room 数据库及其 DAO
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // 将各 Repository 接口绑定到具体实现
    @Binds
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    abstract fun bindBehaviorRepository(impl: BehaviorRepositoryImpl): BehaviorRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindActivityManagementRepository(
        impl: ActivityManagementRepositoryImpl,
    ): ActivityManagementRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): NLtimerDatabase =
            // 构建 Room 数据库，允许迁移而非销毁重建
            Room.databaseBuilder(
                context,
                NLtimerDatabase::class.java,
                "nltimer-database",
            )
                .fallbackToDestructiveMigration(true)
                .addMigrations(NLtimerDatabase.MIGRATION_3_4, NLtimerDatabase.MIGRATION_4_5, NLtimerDatabase.MIGRATION_5_6, NLtimerDatabase.MIGRATION_7_8, NLtimerDatabase.MIGRATION_8_9)
                .build()

        @Provides
        fun provideActivityDao(database: NLtimerDatabase): ActivityDao =
            database.activityDao()

        @Provides
        fun provideActivityGroupDao(database: NLtimerDatabase): ActivityGroupDao =
            database.activityGroupDao()

        @Provides
        fun provideTagDao(database: NLtimerDatabase): TagDao =
            database.tagDao()

        @Provides
        fun provideBehaviorDao(database: NLtimerDatabase): BehaviorDao =
            database.behaviorDao()

        @Provides
        @Singleton
        fun provideClockService(): ClockService = SystemClockService()
    }
}
