package com.nltimer.core.tools

/**
 * 工具错误定义
 *
 * 所有错误统一携带 [code]（机器可读，用于 i18n / 日志聚合 / AI 决策）
 * 与 [message]（人类可读，可直接展示）
 */
sealed class ToolError {
    abstract val code: String
    abstract val message: String

    /** 参数验证错误 —— 入参缺失、格式不符或越界 */
    data class ValidationError(override val message: String) : ToolError() {
        override val code: String = CODE
        companion object { const val CODE = "VALIDATION_ERROR" }
    }

    /** 权限不足 —— 调用方权限低于工具要求 */
    data class PermissionDenied(override val message: String) : ToolError() {
        override val code: String = CODE
        companion object { const val CODE = "PERMISSION_DENIED" }
    }

    /** 资源未找到 —— 例如未注册的工具名或不存在的实体 */
    data class NotFound(override val message: String) : ToolError() {
        override val code: String = CODE
        companion object { const val CODE = "NOT_FOUND" }
    }

    /** 网络错误 —— 远端调用类工具使用 */
    data class NetworkError(override val message: String) : ToolError() {
        override val code: String = CODE
        companion object { const val CODE = "NETWORK_ERROR" }
    }

    /** 超时 —— 工具执行时间超过 [ToolRegistry.executeTool] 的 timeoutMillis */
    data class TimeoutError(override val message: String) : ToolError() {
        override val code: String = CODE
        companion object { const val CODE = "TIMEOUT" }
    }

    /** 内部错误 —— 未预料的异常被注册表捕获后归一化 */
    data class InternalError(override val message: String) : ToolError() {
        override val code: String = CODE
        companion object { const val CODE = "INTERNAL_ERROR" }
    }
}
