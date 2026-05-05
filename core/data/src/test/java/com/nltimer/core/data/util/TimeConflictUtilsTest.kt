package com.nltimer.core.data.util

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeConflictUtilsTest {

    private fun createBehavior(
        id: Long,
        startTime: Long,
        endTime: Long?,
        status: BehaviorNature,
    ) = Behavior(
        id = id,
        activityId = 1,
        startTime = startTime,
        endTime = endTime,
        status = status,
        note = null,
        pomodoroCount = 0,
        sequence = 0,
        estimatedDuration = null,
        actualDuration = null,
        achievementLevel = null,
        wasPlanned = false,
    )

    @Test
    fun `no conflict when intervals do not overlap`() {
        val existing = listOf(createBehavior(1, 1000, 2000, BehaviorNature.COMPLETED))
        assertFalse(hasTimeConflict(2000, 3000, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `no conflict when boundary touches`() {
        val existing = listOf(createBehavior(1, 1000, 2000, BehaviorNature.COMPLETED))
        assertFalse(hasTimeConflict(2000, 3000, BehaviorNature.COMPLETED, existing))
        assertFalse(hasTimeConflict(0, 1000, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `conflict when intervals overlap`() {
        val existing = listOf(createBehavior(1, 1000, 3000, BehaviorNature.COMPLETED))
        assertTrue(hasTimeConflict(2000, 4000, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `conflict when new interval contains existing`() {
        val existing = listOf(createBehavior(1, 2000, 3000, BehaviorNature.COMPLETED))
        assertTrue(hasTimeConflict(1000, 4000, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `conflict when existing interval contains new`() {
        val existing = listOf(createBehavior(1, 1000, 4000, BehaviorNature.COMPLETED))
        assertTrue(hasTimeConflict(2000, 3000, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `pending behavior does not conflict`() {
        val existing = listOf(createBehavior(1, 1000, 2000, BehaviorNature.COMPLETED))
        assertFalse(hasTimeConflict(1500, 2500, BehaviorNature.PENDING, existing))
    }

    @Test
    fun `active behavior conflicts with any future interval`() {
        val existing = listOf(createBehavior(1, 1000, null, BehaviorNature.ACTIVE))
        assertTrue(hasTimeConflict(2000, 3000, BehaviorNature.COMPLETED, existing, currentTime = 5000))
    }

    @Test
    fun `active behavior does not conflict with past interval`() {
        val existing = listOf(createBehavior(1, 3000, null, BehaviorNature.ACTIVE))
        assertFalse(hasTimeConflict(1000, 2000, BehaviorNature.COMPLETED, existing, currentTime = 5000))
    }

    @Test
    fun `completed without endTime returns false`() {
        val existing = listOf(createBehavior(1, 1000, 2000, BehaviorNature.COMPLETED))
        assertFalse(hasTimeConflict(3000, null, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `ignore specified behavior id`() {
        val existing = listOf(createBehavior(1, 1000, 3000, BehaviorNature.COMPLETED))
        assertFalse(hasTimeConflict(2000, 4000, BehaviorNature.COMPLETED, existing, ignoreBehaviorId = 1))
    }

    @Test
    fun `zero length interval returns false`() {
        val existing = listOf(createBehavior(1, 1000, 2000, BehaviorNature.COMPLETED))
        assertFalse(hasTimeConflict(3000, 3000, BehaviorNature.COMPLETED, existing))
    }

    @Test
    fun `cross day behavior conflicts`() {
        val existing = listOf(createBehavior(1, 1000, 3000, BehaviorNature.COMPLETED))
        assertTrue(hasTimeConflict(2000, 4000, BehaviorNature.COMPLETED, existing))
    }
}
