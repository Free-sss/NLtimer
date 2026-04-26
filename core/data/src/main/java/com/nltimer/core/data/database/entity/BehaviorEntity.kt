package com.nltimer.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "behaviors",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("activityId"), Index("startTime"), Index("status"), Index("sequence")],
)
data class BehaviorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val status: String = "pending",
    val note: String? = null,
    val pomodoroCount: Int = 0,
    val sequence: Int = 0,
    val estimatedDuration: Long? = null,
    val actualDuration: Long? = null,
    val achievementLevel: Int? = null,
    val wasPlanned: Boolean = false,
)
