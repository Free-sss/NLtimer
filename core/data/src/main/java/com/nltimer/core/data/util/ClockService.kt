package com.nltimer.core.data.util

interface ClockService {
    fun currentTimeMillis(): Long
}

class SystemClockService : ClockService {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
