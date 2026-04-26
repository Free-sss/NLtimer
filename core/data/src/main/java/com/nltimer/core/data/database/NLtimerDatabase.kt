package com.nltimer.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nltimer.core.data.database.dao.ActivityDao
import com.nltimer.core.data.database.dao.BehaviorDao
import com.nltimer.core.data.database.dao.TagDao
import com.nltimer.core.data.database.entity.ActivityEntity
import com.nltimer.core.data.database.entity.ActivityTagBindingEntity
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.database.entity.TagEntity

@Database(
    entities = [
        ActivityEntity::class,
        TagEntity::class,
        BehaviorEntity::class,
        ActivityTagBindingEntity::class,
        BehaviorTagCrossRefEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class NLtimerDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun tagDao(): TagDao
    abstract fun behaviorDao(): BehaviorDao
}
