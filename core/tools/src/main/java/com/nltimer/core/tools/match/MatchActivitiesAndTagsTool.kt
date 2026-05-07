package com.nltimer.core.tools.match

import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.tools.AccessLevel
import com.nltimer.core.tools.ErrorExample
import com.nltimer.core.tools.ParameterConstraint
import com.nltimer.core.tools.ParameterType
import com.nltimer.core.tools.ToolCategory
import com.nltimer.core.tools.ToolDefinition
import com.nltimer.core.tools.ToolDocumentation
import com.nltimer.core.tools.ToolError
import com.nltimer.core.tools.ToolParameter
import com.nltimer.core.tools.ToolResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.first

/**
 * 工具：用关键词或正则匹配活动 / 标签（UX 导向，非 AI 专用）
 *
 * 业务场景：
 * - 用户在搜索框输入文字 → 自动高亮 / 选中匹配到的活动与标签
 * - 也能被 AI Agent 调用以快速定位用户提到的实体
 *
 * 匹配策略：
 * - 默认对每一项优先匹配 `keywords` 字段（按 `,` `;` `空白` 拆分多个候选）
 * - 当某项 `keywords` 为空 / null 时，回退到匹配 `name` 字段
 * - useRegex=true 时，query 解析为 [Regex]；解析失败返回 ValidationError
 * - useRegex=false 时（默认），按 substring 匹配，受 caseSensitive 控制
 * - 默认排除已归档项（includeArchived=true 才纳入）
 *
 * 返回 Map<String, Any> 结构：
 * ```
 * {
 *   "query": "...",
 *   "useRegex": false,
 *   "activities": [{ "id": 1, "name": "跑步", "matchedField": "keywords" }, ...],
 *   "tags":       [{ "id": 1, "name": "运动", "matchedField": "name" }, ...]
 * }
 * ```
 */
@Singleton
class MatchActivitiesAndTagsTool @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val tagRepository: TagRepository,
) : ToolDefinition {

    override val name: String = "matchActivitiesAndTags"
    override val description: String =
        "用关键词或正则匹配活动 / 标签；优先 keywords 字段，空则回退 name；UX 用，AI 也可调用"
    override val category: ToolCategory = ToolCategory.ACTIVITIES
    override val accessLevel: AccessLevel = AccessLevel.READ
    override val returnType: KClass<*> = Map::class

    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(
            name = "query",
            description = "查询文本（substring 或 regex，取决于 useRegex）",
            type = ParameterType.STRING,
            required = true,
            constraints = ParameterConstraint(minLength = 1, maxLength = MAX_QUERY_LENGTH),
        ),
        ToolParameter(
            name = "useRegex",
            description = "true 时把 query 当作正则解析；解析失败返回 ValidationError",
            type = ParameterType.BOOLEAN,
            required = false,
            default = false,
        ),
        ToolParameter(
            name = "caseSensitive",
            description = "是否区分大小写；仅在 useRegex=false 时生效",
            type = ParameterType.BOOLEAN,
            required = false,
            default = false,
        ),
        ToolParameter(
            name = "scope",
            description = "搜索范围：activities / tags / both",
            type = ParameterType.STRING,
            required = false,
            default = SCOPE_BOTH,
            constraints = ParameterConstraint(
                enum = listOf(SCOPE_ACTIVITIES, SCOPE_TAGS, SCOPE_BOTH),
            ),
        ),
        ToolParameter(
            name = "includeArchived",
            description = "是否包含已归档项；默认 false 排除",
            type = ParameterType.BOOLEAN,
            required = false,
            default = false,
        ),
    )

    override suspend fun execute(args: Map<String, Any?>): ToolResult {
        val query = args["query"] as? String
            ?: return ToolResult.Error(name, ToolError.ValidationError("query 必须是字符串"))
        val useRegex = (args["useRegex"] as? Boolean) ?: false
        val caseSensitive = (args["caseSensitive"] as? Boolean) ?: false
        val scope = (args["scope"] as? String) ?: SCOPE_BOTH
        val includeArchived = (args["includeArchived"] as? Boolean) ?: false

        val matcher: Matcher = try {
            buildMatcher(query, useRegex, caseSensitive)
        } catch (e: IllegalArgumentException) {
            return ToolResult.Error(name, ToolError.ValidationError(e.message ?: "正则解析失败"))
        }

        return runCatching {
            val matchedActivities = if (scope == SCOPE_TAGS) {
                emptyList()
            } else {
                val source = if (includeArchived) {
                    activityRepository.getAll().first()
                } else {
                    activityRepository.getAllActive().first()
                }
                source.mapNotNull { activity ->
                    matchItem(matcher, activity.keywords, activity.name)?.let { field ->
                        mapOf(
                            "id" to activity.id,
                            "name" to activity.name,
                            "matchedField" to field,
                        )
                    }
                }
            }

            val matchedTags = if (scope == SCOPE_ACTIVITIES) {
                emptyList()
            } else {
                val source = if (includeArchived) {
                    tagRepository.getAll().first()
                } else {
                    tagRepository.getAllActive().first()
                }
                source.mapNotNull { tag ->
                    matchItem(matcher, tag.keywords, tag.name)?.let { field ->
                        mapOf(
                            "id" to tag.id,
                            "name" to tag.name,
                            "matchedField" to field,
                        )
                    }
                }
            }

            ToolResult.Success(
                name = name,
                data = mapOf(
                    "query" to query,
                    "useRegex" to useRegex,
                    "activities" to matchedActivities,
                    "tags" to matchedTags,
                ),
            )
        }.getOrElse { e ->
            ToolResult.Error(name, ToolError.InternalError(e.message ?: "匹配失败"))
        }
    }

    /**
     * 对单个项执行匹配逻辑：优先 keywords 字段，空则回退 name
     * 返回命中的字段名（"keywords" 或 "name"），未命中返回 null
     */
    private fun matchItem(matcher: Matcher, keywords: String?, name: String): String? {
        val candidates = parseKeywords(keywords)
        if (candidates.isNotEmpty()) {
            // keywords 非空：仅在 keywords 中找；找不到不回退（明确 UX 语义）
            return if (candidates.any { matcher.matches(it) }) "keywords" else null
        }
        // keywords 为空：回退到 name
        return if (matcher.matches(name)) "name" else null
    }

    /** 把 keywords 字符串拆分为多个候选；逗号 / 分号 / 空白均可作分隔 */
    private fun parseKeywords(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(',', '，', ';', '；', ' ', '\t', '\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    /** 构造统一的匹配器；正则模式下解析失败抛 IllegalArgumentException */
    @Throws(IllegalArgumentException::class)
    private fun buildMatcher(query: String, useRegex: Boolean, caseSensitive: Boolean): Matcher {
        return if (useRegex) {
            val options = if (caseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
            val regex = try {
                Regex(query, options)
            } catch (e: Exception) {
                throw IllegalArgumentException("正则解析失败: ${e.message}")
            }
            Matcher { candidate -> regex.containsMatchIn(candidate) }
        } else {
            val needle = if (caseSensitive) query else query.lowercase()
            Matcher { candidate ->
                val haystack = if (caseSensitive) candidate else candidate.lowercase()
                needle in haystack
            }
        }
    }

    private fun interface Matcher {
        fun matches(candidate: String): Boolean
    }

    override fun getDocumentation(): ToolDocumentation = ToolDocumentation(
        name = name,
        description = description,
        category = category,
        accessLevel = accessLevel,
        parameters = parameters,
        returnExample = """
            {
              "query": "跑",
              "useRegex": false,
              "activities": [
                { "id": 7, "name": "跑步", "matchedField": "keywords" }
              ],
              "tags": [
                { "id": 3, "name": "运动", "matchedField": "name" }
              ]
            }
        """.trimIndent(),
        errorExamples = listOf(
            ErrorExample(
                code = "VALIDATION_ERROR",
                message = "正则解析失败: ...",
                scenario = "useRegex=true 但 query 是非法正则",
            ),
            ErrorExample(
                code = "VALIDATION_ERROR",
                message = "scope must be one of [activities, tags, both]",
                scenario = "传入未支持的 scope 值",
            ),
        ),
        usageExamples = listOf(
            "matchActivitiesAndTags(query=\"跑\")",
            "matchActivitiesAndTags(query=\"^运动\", useRegex=true)",
            "matchActivitiesAndTags(query=\"work\", scope=\"tags\", caseSensitive=true)",
        ),
    )

    private companion object {
        const val MAX_QUERY_LENGTH = 200
        const val SCOPE_ACTIVITIES = "activities"
        const val SCOPE_TAGS = "tags"
        const val SCOPE_BOTH = "both"
    }
}
