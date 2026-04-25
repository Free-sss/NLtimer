package com.nltimer.feature.timer.repository

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TimerRepositoryTest {

    private val repository = TimerRepository()

    @Test
    fun timerFlow_emitsIncrementingSeconds() = runTest {
        val results = repository.timerFlow().take(3).toList()
        assertEquals(listOf(1L, 2L, 3L), results)
    }
}
