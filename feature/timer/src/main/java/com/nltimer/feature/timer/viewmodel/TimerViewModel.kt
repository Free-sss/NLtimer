package com.nltimer.feature.timer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nltimer.feature.timer.model.TimerState
import com.nltimer.feature.timer.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerState())
    val uiState: StateFlow<TimerState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun toggleTimer() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update { TimerState() }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            val startFrom = _uiState.value.elapsedSeconds
            repository.timerFlow()
                .collect { seconds ->
                    _uiState.update {
                        it.copy(elapsedSeconds = startFrom + seconds, isRunning = true)
                    }
                }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }
}
