package com.nltimer.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BehaviorCalculatorTest {

    @Test
    fun `planned behavior completed on time returns achievement level 100`() {
        val startTime = 0L
        val endTime = 30 * 60_000L
        val result = BehaviorCalculator.calculateCompletion(
            startTime = startTime,
            endTime = endTime,
            wasPlanned = true,
            estimatedDurationMinutes = 30,
        )
        assertEquals(30 * 60_000L, result.durationMs)
        assertEquals(100, result.achievementLevel)
    }

    @Test
    fun `planned behavior exceeded by 50 percent returns achievement level 50`() {
        val startTime = 0L
        val endTime = 45 * 60_000L
        val result = BehaviorCalculator.calculateCompletion(
            startTime = startTime,
            endTime = endTime,
            wasPlanned = true,
            estimatedDurationMinutes = 30,
        )
        assertEquals(45 * 60_000L, result.durationMs)
        assertEquals(50, result.achievementLevel)
    }

    @Test
    fun `planned behavior finished 50 percent early returns achievement level 50`() {
        val startTime = 0L
        val endTime = 15 * 60_000L
        val result = BehaviorCalculator.calculateCompletion(
            startTime = startTime,
            endTime = endTime,
            wasPlanned = true,
            estimatedDurationMinutes = 30,
        )
        assertEquals(15 * 60_000L, result.durationMs)
        assertEquals(50, result.achievementLevel)
    }

    @Test
    fun `unplanned behavior returns null achievement level`() {
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 0L,
            endTime = 30 * 60_000L,
            wasPlanned = false,
            estimatedDurationMinutes = 30,
        )
        assertEquals(30 * 60_000L, result.durationMs)
        assertNull(result.achievementLevel)
    }

    @Test
    fun `no estimated duration returns null achievement level`() {
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 0L,
            endTime = 30 * 60_000L,
            wasPlanned = true,
            estimatedDurationMinutes = null,
        )
        assertEquals(30 * 60_000L, result.durationMs)
        assertNull(result.achievementLevel)
    }

    @Test
    fun `negative duration clamped to zero`() {
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 1000L,
            endTime = 500L,
            wasPlanned = true,
            estimatedDurationMinutes = 30,
        )
        assertEquals(0L, result.durationMs)
    }

    @Test
    fun `zero estimated duration returns null achievement level`() {
        val result = BehaviorCalculator.calculateCompletion(
            startTime = 0L,
            endTime = 30 * 60_000L,
            wasPlanned = true,
            estimatedDurationMinutes = 0,
        )
        assertEquals(30 * 60_000L, result.durationMs)
        assertNull(result.achievementLevel)
    }
}
