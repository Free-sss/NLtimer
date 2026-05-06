package com.nltimer.core.data.util

import java.time.format.DateTimeFormatter

/** HH:mm 格式的时间格式化器，供各视图共享使用。 */
val hhmmFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * 将毫秒格式化为"X时X分X秒"的可读字符串。
 */
fun formatDuration(ms: Long): String {
    val hours = ms / 3600000
    val minutes = (ms % 3600000) / 60000
    val seconds = (ms % 60000) / 1000
    return buildString {
        if (hours > 0) append("${hours}时")
        if (minutes > 0 || hours > 0) append("${minutes}分")
        if (hours == 0L) append("${seconds}秒")
    }
}

/**
 * 将分钟数格式化为"X小时X分钟"的可读字符串。
 */
fun formatDurationMinutes(minutes: Long): String {
    if (minutes == 0L) return "0 分钟"
    val hours = minutes / 60
    val mins = minutes % 60
    return buildString {
        if (hours > 0) append("${hours} 小时 ")
        if (mins > 0 || hours == 0L) append("${mins} 分钟")
    }.trim()
}
