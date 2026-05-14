package com.nltimer.core.tools

/**
 * 工具执行结果
 *
 * 所有工具调用均返回此密封类，绝不向外抛异常；调用方通过 when 分支处理 Success / Error
 */
sealed class ToolResult {

    /** 调用的工具名 */
    abstract val name: String

    /** 执行完成时间戳（毫秒） */
    abstract val executedAt: Long

    /** 成功结果 */
    data class Success(
        override val name: String,
        /** 业务返回数据；类型由具体工具决定，调用方需按文档转型 */
        val data: Any?,
        override val executedAt: Long = System.currentTimeMillis(),
    ) : ToolResult()

    /** 失败结果 */
    data class Error(
        override val name: String,
        val error: ToolError,
        override val executedAt: Long = System.currentTimeMillis(),
    ) : ToolResult()
}
