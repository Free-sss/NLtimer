package com.nltimer.core.designsystem.theme

import androidx.compose.runtime.Immutable

enum class TimeLabelStyle {
    PILL,
    PLAIN,
    UNDERLINE,
    DOT,
}

enum class TimeLabelFormat {
    HH_MM,
    H_MM,
    H_MM_A,
}

@Immutable
data class TimeLabelConfig(
    val visible: Boolean = true,
    val style: TimeLabelStyle = TimeLabelStyle.PILL,
    val format: TimeLabelFormat = TimeLabelFormat.HH_MM,
)

fun TimeLabelStyle.toDisplayString(): String = when (this) {
    TimeLabelStyle.PILL -> "药丸"
    TimeLabelStyle.PLAIN -> "纯文字"
    TimeLabelStyle.UNDERLINE -> "下划线"
    TimeLabelStyle.DOT -> "圆点"
}

fun TimeLabelFormat.toDisplayString(): String = when (this) {
    TimeLabelFormat.HH_MM -> "HH:mm"
    TimeLabelFormat.H_MM -> "H:mm"
    TimeLabelFormat.H_MM_A -> "h:mm a"
}
