package com.nltimer.core.tools.match

/**
 * `HomeViewModel.processNote` 输出包：cleanedNote + directive 处理结果 + 反向扫描结果。
 *
 * 提到顶级类型而非嵌套在 ViewModel 中，是为了让 `core/behaviorui` 的 sheet 链
 * 能引用其作为 `onProcessNote` 回调返回类型，避免形成 core/behaviorui → feature/home
 * 的循环依赖。
 */
data class NoteProcessOutcome(
    val cleanedNote: String,
    val directiveOutcome: ApplyNoteDirectivesUseCase.Outcome,
    val scanResult: NoteScanResult,
) {
    companion object {
        val Empty = NoteProcessOutcome(
            cleanedNote = "",
            directiveOutcome = ApplyNoteDirectivesUseCase.Outcome.Empty,
            scanResult = NoteScanResult(null, emptySet()),
        )
    }
}
