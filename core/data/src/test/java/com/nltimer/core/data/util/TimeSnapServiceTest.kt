package com.nltimer.core.data.util

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TimeSnapServiceTest {

    private lateinit var service: TimeSnapService

    @BeforeEach
    fun setUp() {
        service = TimeSnapService()
    }

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
    fun `no snap and no conflict when no overlapping behaviors`() {
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = emptyList(),
            currentTime = 30_000,
        )
        assertEquals(10_000, result.adjustedStart)
        assertEquals(20_000, result.adjustedEnd)
        assertFalse(result.hasConflict)
    }

    @Test
    fun `snap start to prevEnd plus 1 when overlapping prev behavior`() {
        val prev = createBehavior(1, 5_000, 10_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(prev),
            currentTime = 30_000,
        )
        assertEquals(10_001, result.adjustedStart)
        assertFalse(result.hasConflict)
    }

    @Test
    fun `adjust endTime to minute boundary 999 when in same minute as prevEnd`() {
        val prevEnd = 60_000L + 30_000L
        val prev = createBehavior(1, 60_000, prevEnd, BehaviorNature.COMPLETED)
        val newEnd = 60_000L + 45_000L
        val result = service.snapAndCheckConflict(
            newStart = 60_000L,
            newEnd = newEnd,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(prev),
            currentTime = 120_000,
        )
        assertEquals(prevEnd + 1, result.adjustedStart)
        assertEquals(60_000L * 2 - 1, result.adjustedEnd)
    }

    @Test
    fun `keep original endTime when crossing minute boundary`() {
        val prevEnd = 60_000L + 30_000L
        val prev = createBehavior(1, 60_000, prevEnd, BehaviorNature.COMPLETED)
        val newEnd = 120_000L + 15_000L
        val result = service.snapAndCheckConflict(
            newStart = 60_000L,
            newEnd = newEnd,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(prev),
            currentTime = 200_000,
        )
        assertEquals(prevEnd + 1, result.adjustedStart)
        assertEquals(newEnd, result.adjustedEnd)
    }

    @Test
    fun `hasConflict true when real overlap exists`() {
        val existing = createBehavior(1, 15_000, 25_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(existing),
            currentTime = 30_000,
        )
        assertTrue(result.hasConflict)
    }

    @Test
    fun `PENDING behavior does not snap or check conflict`() {
        val existing = createBehavior(1, 5_000, 15_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.PENDING,
            overlappingBehaviors = listOf(existing),
            currentTime = 30_000,
        )
        assertEquals(10_000, result.adjustedStart)
        assertEquals(20_000, result.adjustedEnd)
        assertFalse(result.hasConflict)
    }

    @Test
    fun `ignoreBehaviorId excludes specified behavior from conflict check`() {
        val existing = createBehavior(1, 15_000, 25_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(existing),
            currentTime = 30_000,
            ignoreBehaviorId = 1,
        )
        assertFalse(result.hasConflict)
    }

    @Test
    fun `ACTIVE behavior with null newEnd checks conflict with infinite end`() {
        val existing = createBehavior(1, 20_000, 30_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = null,
            newStatus = BehaviorNature.ACTIVE,
            overlappingBehaviors = listOf(existing),
            currentTime = 50_000,
        )
        assertTrue(result.hasConflict)
    }

    @Test
    fun `snap adjusts start when prevEnd equals newStart`() {
        val prev = createBehavior(1, 0, 10_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(prev),
            currentTime = 30_000,
        )
        assertEquals(10_001, result.adjustedStart)
    }

    @Test
    fun `multiple overlapping behaviors snap uses max endTime`() {
        val prev1 = createBehavior(1, 0, 8_000, BehaviorNature.COMPLETED)
        val prev2 = createBehavior(2, 0, 12_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = listOf(prev1, prev2),
            currentTime = 30_000,
        )
        assertEquals(12_001, result.adjustedStart)
    }

    @Test
    fun `empty overlapping list returns original times`() {
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.COMPLETED,
            overlappingBehaviors = emptyList(),
            currentTime = 30_000,
        )
        assertEquals(10_000, result.adjustedStart)
        assertEquals(20_000, result.adjustedEnd)
        assertFalse(result.hasConflict)
    }

    @Test
    fun `PENDING with overlapping behaviors still no conflict`() {
        val existing = createBehavior(1, 5_000, 25_000, BehaviorNature.COMPLETED)
        val result = service.snapAndCheckConflict(
            newStart = 10_000,
            newEnd = 20_000,
            newStatus = BehaviorNature.PENDING,
            overlappingBehaviors = listOf(existing),
            currentTime = 30_000,
        )
        assertFalse(result.hasConflict)
    }
}
