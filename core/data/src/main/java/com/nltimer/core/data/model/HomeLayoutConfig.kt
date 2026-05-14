package com.nltimer.core.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class HomeLayoutConfig(
    val grid: GridLayoutStyle = GridLayoutStyle(),
    val log: LogLayoutStyle = LogLayoutStyle(),
    val timeline: TimelineLayoutStyle = TimelineLayoutStyle(),
    val moment: MomentLayoutStyle = MomentLayoutStyle(),
)

@Immutable
data class GridLayoutStyle(
    val columns: Int = 4,
    val minRowHeight: Int = 100,
    val maxCellHeight: Int = 140,
    val columnSpacing: Int = 5,
    val cellPadding: Int = 4,
    val iconSize: Int = 14,
    val tagScale: Float = 0.8f,
    val tagSpacing: Int = 2,
    val activeBgAlpha: Float = 0.3f,
)

@Immutable
data class LogLayoutStyle(
    val cardPadding: Int = 12,
    val iconSize: Int = 18,
    val iconSpacing: Int = 6,
    val tagRowSpacing: Int = 6,
    val statusBadgePaddingH: Int = 8,
    val statusBadgePaddingV: Int = 2,
)

@Immutable
data class TimelineLayoutStyle(
    val itemSpacing: Int = 8,
)

@Immutable
data class MomentLayoutStyle(
    val cardPadding: Int = 16,
)
