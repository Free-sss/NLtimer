package com.nltimer.core.tools.timing

import com.nltimer.core.data.repository.BehaviorRepository
import com.nltimer.core.tools.AccessLevel
import com.nltimer.core.tools.ErrorExample
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
 * 工具：查询当前正在进行（ACTIVE）的行为记录
 *
 * 业务场景：
 * - 主屏顶部"我现在在做什么"
 * - AI Agent 决策前的状态快照
 *
 * 返回 Behavior?；没有正在进行的行为时返回 null（仍是 Success，data = null）
 */
@Singleton
class QueryCurrentBehaviorTool @Inject constructor(
    private val behaviorRepository: BehaviorRepository,
) : ToolDefinition {

    override val name: String = "queryCurrentBehavior"
    override val description: String = "查询当前正在进行的行为记录；没有则返回 null"
    override val category: ToolCategory = ToolCategory.TIMING
    override val accessLevel: AccessLevel = AccessLevel.READ
    override val parameters: List<ToolParameter> = emptyList()

    // KClass 不直接表达可空类型，这里返回 Any::class，由文档说明真实类型为 Behavior?
    override val returnType: KClass<*> = Any::class

    override suspend fun execute(args: Map<String, Any?>): ToolResult {
        return runCatching {
            val current = behaviorRepository.getCurrentBehavior().first()
            ToolResult.Success(name, current)
        }.getOrElse { e ->
            ToolResult.Error(
                name = name,
                error = ToolError.InternalError(e.message ?: "查询当前行为失败"),
            )
        }
    }

    override fun getDocumentation(): ToolDocumentation = ToolDocumentation(
        name = name,
        description = description,
        category = category,
        accessLevel = accessLevel,
        parameters = parameters,
        returnExample = """
            {
              "id": 42,
              "activityId": 1,
              "startTime": 1714896000000,
              "endTime": null,
              "status": "ACTIVE"
            }
            // 或 null（无正在进行的行为）
        """.trimIndent(),
        errorExamples = listOf(
            ErrorExample(
                code = "INTERNAL_ERROR",
                message = "查询当前行为失败",
                scenario = "数据库读取异常",
            ),
        ),
        usageExamples = listOf(
            "queryCurrentBehavior()  // 无参",
        ),
    )
}
