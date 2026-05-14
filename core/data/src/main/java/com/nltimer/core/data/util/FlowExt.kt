package com.nltimer.core.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 将 Flow<List<E>> 中的每个元素通过 [transform] 转换为 Flow<List<M>>。
 *
 * 等价于 `.map { list -> list.map { transform(it) } }`，减少样板代码。
 *
 * @param E 源列表元素类型
 * @param M 目标列表元素类型
 * @param transform 单个元素的转换函数
 * @return 转换后的 Flow
 */
fun <E, M> Flow<List<E>>.mapList(transform: (E) -> M): Flow<List<M>> =
    map { list -> list.map { transform(it) } }
