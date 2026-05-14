package com.nltimer.core.tools.timing

import com.nltimer.core.data.model.Behavior
import com.nltimer.core.data.model.BehaviorNature
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.BehaviorRepository
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
 * 工具：开始一段计时（写入新的 ACTIVE Behavior）
 *
 * 入参：
 * - activityId (必填，Number, >= 1) —— 关联的活动 id
 * - note (可选，String, maxLength=500) —— 备注
 *
 * 业务约束：
 * - activityId 对应活动必须存在
 * - 当前不能已有 ACTIVE 行为（避免双开计时；如需切换应先调用结束工具）
 *
 * 返回新 Behavior 的 id (Long)
 */
@Singleton
class StartBehaviorTool @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val behaviorRepository: BehaviorRepository,
) : ToolDefinition {

    override val name: String = "startBehavior"
    override val description: String = "为指定活动开始一段计时（写入 ACTIVE 状态的 Behavior）"
    override val category: ToolCategory = ToolCategory.TIMING
    override val accessLevel: AccessLevel = AccessLevel.WRITE

    override val parameters: List<ToolParameter> = listOf(
        ToolParameter(
            name = "activityId",
            description = "活动 id（来自 listActivities 返回的元素之一）",
            type = ParameterType.NUMBER,
            required = true,
            constraints = ParameterConstraint(minValue = 1),
        ),
        ToolParameter(
            name = "note",
            description = "可选备注，最长 500 字",
            type = ParameterType.STRING,
            required = false,
            constraints = ParameterConstraint(maxLength = MAX_NOTE_LENGTH),
        ),
    )

    override val returnType: KClass<*> = Long::class

    override suspend fun execute(args: Map<String, Any?>): ToolResult {
        val activityId = (args["activityId"] as? Number)?.toLong()
            ?: return ToolResult.Error(
                name = name,
                error = ToolError.ValidationError("activityId 必须是数字"),
            )
        val note = args["note"] as? String

        return runCatching {
            val activity = activityRepository.getById(activityId)
                ?: return@runCatching ToolResult.Error(
                    name = name,
                    error = ToolError.NotFound("活动不存在: id=$activityId"),
                )

            val current = behaviorRepository.getCurrentBehavior().first()
            if (current != null) {
                return@runCatching ToolResult.Error(
                    name = name,
                    error = ToolError.ValidationError(
                        "当前已有正在进行的行为 (id=${current.id})，请先结束",
                    ),
                )
            }

            val behavior = Behavior(
                id = 0L,
                activityId = activity.id,
                startTime = System.currentTimeMillis(),
                endTime = null,
                status = BehaviorNature.ACTIVE,
                note = note,
                pomodoroCount = 0,
                sequence = 0,
                estimatedDuration = null,
                actualDuration = null,
                achievementLevel = null,
                wasPlanned = false,
            )
            val newId = behaviorRepository.insert(behavior, emptyList())
            ToolResult.Success(name, newId)
        }.getOrElse { e ->
            ToolResult.Error(
                name = name,
                error = ToolError.InternalError(e.message ?: "开始计时失败"),
            )
        }
    }

    override fun getDocumentation(): ToolDocumentation = ToolDocumentation(
        name = name,
        description = description,
        category = category,
        accessLevel = accessLevel,
        parameters = parameters,
        returnExample = "42  // 新 Behavior 的 id",
        errorExamples = listOf(
            ErrorExample(
                code = "VALIDATION_ERROR",
                message = "activityId 必须是数字",
                scenario = "调用方传入非数字 activityId",
            ),
            ErrorExample(
                code = "NOT_FOUND",
                message = "活动不存在: id=999",
                scenario = "传入的 activityId 在数据库中不存在",
            ),
            ErrorExample(
                code = "VALIDATION_ERROR",
                message = "当前已有正在进行的行为 (id=41)，请先结束",
                scenario = "已有 ACTIVE 行为时不允许再次启动",
            ),
        ),
        usageExamples = listOf(
            "startBehavior(activityId=1)",
            "startBehavior(activityId=2, note=\"番茄钟第一阶段\")",
        ),
    )

    private companion object {
        const val MAX_NOTE_LENGTH = 500
    }
}
