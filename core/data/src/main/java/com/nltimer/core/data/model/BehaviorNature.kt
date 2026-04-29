package com.nltimer.core.data.model

/**
 * BehaviorNature 行为状态枚举
 * 定义行为记录的三种状态：待开始、进行中、已完成
 *
 * @param key 与数据库持久化对应的字符串标识
 */
enum class BehaviorNature(val key: String) {
    PENDING("pending"),
    ACTIVE("active"),
    COMPLETED("completed"),
}
