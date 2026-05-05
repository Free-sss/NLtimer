package com.nltimer.core.data.util

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
