package com.nltimer.core.tools.match

/**
 * Match 类工具的共享辅助：
 * - keywords 字符串拆分
 * - 优先 keywords / 空回退 name 的统一项匹配模板
 * - 抽象 [FieldMatcher] 让搜索（substring）/选择（exact token）两种模式注入不同实现
 *
 * 仅 internal 暴露，不进入工具公共 API。
 */

/** 把 keywords 字符串拆分为多个候选；逗号 / 分号 / 空白均可作分隔 */
internal fun parseKeywords(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.split(',', '，', ';', '；', ' ', '\t', '\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

/**
 * 单条候选的匹配判断；不同模式通过不同实现注入：
 * - 搜索模式：substring / regex.containsMatchIn —— 候选包含 query 即可
 * - 选择模式：exact equality / regex.matches —— 候选完整匹配 query 才行
 */
internal fun interface FieldMatcher {
    fun matches(candidate: String): Boolean
}

/**
 * 对单个项执行匹配逻辑：优先 keywords 字段，空则回退 name。
 *
 * 返回命中字段名（"keywords" 或 "name"），未命中返回 null。
 *
 * 语义注意：当 keywords 非空但没有任何候选命中时，**不会**回退到 name —— 这是
 * 显式 UX 约定：用户为某项设置了 keywords 就视为完整意图，不模糊回退。
 */
internal fun matchItem(matcher: FieldMatcher, keywords: String?, name: String): String? {
    val candidates = parseKeywords(keywords)
    if (candidates.isNotEmpty()) {
        return if (candidates.any { matcher.matches(it) }) MATCHED_KEYWORDS else null
    }
    return if (matcher.matches(name)) MATCHED_NAME else null
}

internal const val MATCHED_KEYWORDS = "keywords"
internal const val MATCHED_NAME = "name"
internal const val SCOPE_ACTIVITIES = "activities"
internal const val SCOPE_TAGS = "tags"
internal const val SCOPE_BOTH = "both"
internal const val MAX_QUERY_LENGTH = 200
