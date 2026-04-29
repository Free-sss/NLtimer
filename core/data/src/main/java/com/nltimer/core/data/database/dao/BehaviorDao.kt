package com.nltimer.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nltimer.core.data.database.entity.BehaviorEntity
import com.nltimer.core.data.database.entity.BehaviorTagCrossRefEntity
import com.nltimer.core.data.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * BehaviorDao 行为记录数据访问对象
 * 提供 behaviors 表的基础 CRUD、计时字段更新、状态变更、统计查询及标签关联管理
 */
@Dao
interface BehaviorDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(behavior: BehaviorEntity): Long

    // 单字段更新方法
    @Query("UPDATE behaviors SET endTime = :endTime WHERE id = :id")
    suspend fun setEndTime(id: Long, endTime: Long)

    @Query("UPDATE behaviors SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: String)

    @Query("UPDATE behaviors SET startTime = :startTime WHERE id = :id")
    suspend fun setStartTime(id: Long, startTime: Long)

    @Query("UPDATE behaviors SET actualDuration = :duration WHERE id = :id")
    suspend fun setActualDuration(id: Long, duration: Long)

    @Query("UPDATE behaviors SET achievementLevel = :level WHERE id = :id")
    suspend fun setAchievementLevel(id: Long, level: Int)

    @Query("UPDATE behaviors SET sequence = :sequence WHERE id = :id")
    suspend fun setSequence(id: Long, sequence: Int)

    @Query("UPDATE behaviors SET note = :note WHERE id = :id")
    suspend fun setNote(id: Long, note: String?)

    @Query("DELETE FROM behaviors WHERE id = :id")
    suspend fun delete(id: Long)

    /** 查询指定时间范围内的行为记录 */
    @Query(
        """
        SELECT * FROM behaviors
        WHERE startTime >= :dayStart AND startTime < :dayEnd
        ORDER BY sequence ASC, startTime ASC
        """
    )
    fun getByDayRange(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>

    /** 获取当前正在进行的活动（status=active 且未结束） */
    @Query("SELECT * FROM behaviors WHERE status = 'active' AND endTime IS NULL LIMIT 1")
    fun getCurrentBehavior(): Flow<BehaviorEntity?>

    /** 结束所有当前进行中的行为 */
    @Query("UPDATE behaviors SET endTime = :endTime, status = 'completed' WHERE status = 'active' AND endTime IS NULL")
    suspend fun endCurrentBehavior(endTime: Long)

    /** 获取首页展示的行为列表：今日记录 + 所有待办 */
    @Query(
        """
        SELECT * FROM behaviors
        WHERE (startTime >= :dayStart AND startTime < :dayEnd)
           OR status = 'pending'
        ORDER BY sequence ASC
        """
    )
    fun getHomeBehaviors(dayStart: Long, dayEnd: Long): Flow<List<BehaviorEntity>>

    /** 获取排序最靠前的待办行为 */
    @Query("SELECT * FROM behaviors WHERE status = 'pending' ORDER BY sequence ASC LIMIT 1")
    suspend fun getNextPending(): BehaviorEntity?

    @Query("SELECT COALESCE(MAX(sequence), -1) FROM behaviors")
    suspend fun getMaxSequence(): Int

    @Query("SELECT * FROM behaviors WHERE id = :id")
    suspend fun getById(id: Long): BehaviorEntity?

    /** 获取所有待办行为（按排序） */
    @Query("SELECT * FROM behaviors WHERE status = 'pending' ORDER BY sequence ASC")
    fun getPendingBehaviors(): Flow<List<BehaviorEntity>>

    // 统计查询
    @Query("SELECT COUNT(*) FROM behaviors WHERE activityId = :activityId AND status = 'completed'")
    fun getUsageCount(activityId: Long): Flow<Int>

    /** 计算活动总耗时（取 actualDuration，没有则用 endTime - startTime 推算） */
    @Query("SELECT SUM(COALESCE(actualDuration, (endTime - startTime))) FROM behaviors WHERE activityId = :activityId AND status = 'completed'")
    fun getTotalDurationMs(activityId: Long): Flow<Long?>

    @Query("SELECT MAX(startTime) FROM behaviors WHERE activityId = :activityId AND status != 'pending'")
    fun getLastUsedTimestamp(activityId: Long): Flow<Long?>

    // 标签关联操作
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRef(crossRef: BehaviorTagCrossRefEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTagCrossRefs(crossRefs: List<BehaviorTagCrossRefEntity>)

    /** 移除行为与指定标签的关联 */
    @Query(
        """
        DELETE FROM behavior_tag_cross_ref
        WHERE behaviorId = :behaviorId AND tagId IN (:tagIds)
        """
    )
    suspend fun removeTagCrossRefs(behaviorId: Long, tagIds: List<Long>)

    /** 流式查询行为关联的标签 */
    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId
        WHERE btc.behaviorId = :behaviorId
        ORDER BY t.priority DESC, t.name
        """
    )
    fun getTagsForBehavior(behaviorId: Long): Flow<List<TagEntity>>

    /** 批量更新行为字段 */
    @Query(
        """
        UPDATE behaviors
        SET activityId = :activityId,
            startTime = :startTime,
            endTime = :endTime,
            status = :status,
            note = :note
        WHERE id = :id
        """
    )
    suspend fun update(
        id: Long,
        activityId: Long,
        startTime: Long,
        endTime: Long?,
        status: String,
        note: String?,
    )

    /** 删除行为的所有标签关联 */
    @Query("DELETE FROM behavior_tag_cross_ref WHERE behaviorId = :behaviorId")
    suspend fun deleteTagsForBehavior(behaviorId: Long)

    /** 同步查询行为关联的标签 */
    @Query(
        """
        SELECT t.* FROM tags t
        INNER JOIN behavior_tag_cross_ref btc ON t.id = btc.tagId
        WHERE btc.behaviorId = :behaviorId
        ORDER BY t.priority DESC, t.name
        """
    )
    suspend fun getTagsForBehaviorSync(behaviorId: Long): List<TagEntity>
}
