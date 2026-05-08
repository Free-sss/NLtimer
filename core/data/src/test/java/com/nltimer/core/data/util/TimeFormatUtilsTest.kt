package com.nltimer.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatUtilsTest {

    // --- formatDuration ---

    @Test
    fun `formatDuration zero milliseconds`() {
        assertEquals("0秒", formatDuration(0))
    }

    @Test
    fun `formatDuration seconds only`() {
        assertEquals("30秒", formatDuration(30_000))
    }

    @Test
    fun `formatDuration 1 second`() {
        assertEquals("1秒", formatDuration(1000))
    }

    @Test
    fun `formatDuration minutes and seconds`() {
        assertEquals("2分30秒", formatDuration(150_000))
    }

    @Test
    fun `formatDuration minutes only`() {
        assertEquals("5分0秒", formatDuration(300_000))
    }

    @Test
    fun `formatDuration 1 minute exactly`() {
        assertEquals("1分0秒", formatDuration(60_000))
    }

    @Test
    fun `formatDuration hours minutes and seconds`() {
        val ms = 3600_000L + 30 * 60_000 + 45_000
        assertEquals("1时30分", formatDuration(ms))
    }

    @Test
    fun `formatDuration hours and minutes no seconds`() {
        val ms = 2 * 3600_000L + 15 * 60_000
        assertEquals("2时15分", formatDuration(ms))
    }

    @Test
    fun `formatDuration exactly 1 hour`() {
        assertEquals("1时0分", formatDuration(3600_000))
    }

    @Test
    fun `formatDuration exactly 1 hour with seconds`() {
        assertEquals("1时0分", formatDuration(3601_000))
    }

    @Test
    fun `formatDuration large values`() {
        val ms = 100L * 3600_000
        assertEquals("100时0分", formatDuration(ms))
    }

    @Test
    fun `formatDuration 59 seconds`() {
        assertEquals("59秒", formatDuration(59_000))
    }

    @Test
    fun `formatDuration 59 minutes 59 seconds`() {
        val ms = 59 * 60_000L + 59_000
        assertEquals("59分59秒", formatDuration(ms))
    }

    @Test
    fun `formatDuration ignores sub-second remainder`() {
        assertEquals("1秒", formatDuration(1500))
    }

    // --- formatDurationMinutes ---

    @Test
    fun `formatDurationMinutes zero`() {
        assertEquals("0 分钟", formatDurationMinutes(0))
    }

    @Test
    fun `formatDurationMinutes 1 minute`() {
        assertEquals("1 分钟", formatDurationMinutes(1))
    }

    @Test
    fun `formatDurationMinutes 30 minutes`() {
        assertEquals("30 分钟", formatDurationMinutes(30))
    }

    @Test
    fun `formatDurationMinutes 1 hour exactly`() {
        assertEquals("1 小时", formatDurationMinutes(60L))
    }

    @Test
    fun `formatDurationMinutes 1 hour 30 minutes`() {
        assertEquals("1 小时 30 分钟", formatDurationMinutes(90L))
    }

    @Test
    fun `formatDurationMinutes 2 hours`() {
        assertEquals("2 小时", formatDurationMinutes(120))
    }

    @Test
    fun `formatDurationMinutes 2 hours 1 minute`() {
        assertEquals("2 小时 1 分钟", formatDurationMinutes(121))
    }

    @Test
    fun `formatDurationMinutes 59 minutes`() {
        assertEquals("59 分钟", formatDurationMinutes(59))
    }

    @Test
    fun `formatDurationMinutes large value`() {
        assertEquals("48 小时", formatDurationMinutes(2880L))
    }

    @Test
    fun `formatDurationMinutes 61 minutes`() {
        assertEquals("1 小时 1 分钟", formatDurationMinutes(61))
    }

    // --- hhmmFormatter ---

    @Test
    fun `hhmmFormatter formats time correctly`() {
        val time = java.time.LocalTime.of(9, 5)
        assertEquals("09:05", hhmmFormatter.format(time))
    }

    @Test
    fun `hhmmFormatter formats midnight`() {
        val time = java.time.LocalTime.MIDNIGHT
        assertEquals("00:00", hhmmFormatter.format(time))
    }

    @Test
    fun `hhmmFormatter formats 23_59`() {
        val time = java.time.LocalTime.of(23, 59)
        assertEquals("23:59", hhmmFormatter.format(time))
    }

    @Test
    fun `hhmmFormatter formats noon`() {
        val time = java.time.LocalTime.NOON
        assertEquals("12:00", hhmmFormatter.format(time))
    }
}
