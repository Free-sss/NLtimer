package com.nltimer.feature.timer.viewmodel

import com.nltimer.feature.timer.util.MainDispatcherRule
import com.nltimer.feature.timer.model.TimerState
import com.nltimer.feature.timer.repository.TimerRepository
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TimerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TimerRepository()
    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        viewModel = TimerViewModel(repository)
    }

    @Test
    fun initialState_isZeroAndNotRunning() {
        val state = viewModel.uiState.value
        assertEquals(TimerState(elapsedSeconds = 0, isRunning = false), state)
    }

    @Test
    fun toggleTimer_startsTimer() = runTest {
        viewModel.toggleTimer()
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.isRunning)
    }

    @Test
    fun toggleTimer_twice_pausesTimer() = runTest {
        viewModel.toggleTimer()
        advanceUntilIdle()
        viewModel.toggleTimer()
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isRunning)
    }

    @Test
    fun resetTimer_resetsToZero() = runTest {
        viewModel.toggleTimer()
        advanceUntilIdle()
        viewModel.resetTimer()
        advanceUntilIdle()
        assertEquals(TimerState(elapsedSeconds = 0, isRunning = false), viewModel.uiState.value)
    }
}
