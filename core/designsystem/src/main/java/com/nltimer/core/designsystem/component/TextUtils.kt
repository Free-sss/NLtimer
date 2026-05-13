package com.nltimer.core.designsystem.component

fun tagCountLabel(count: Int, emptyLabel: String = "+ 增加"): String =
    if (count == 0) emptyLabel else "$count 个标签"
