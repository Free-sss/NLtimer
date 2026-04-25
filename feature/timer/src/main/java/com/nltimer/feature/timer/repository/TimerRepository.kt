package com.nltimer.feature.timer.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import javax.inject.Inject

class TimerRepository @Inject constructor() {

    fun timerFlow(): Flow<Long> = flow {
        var elapsed = 0L
        while (true) {
            delay(1000)
            emit(++elapsed)
        }
    }
}
