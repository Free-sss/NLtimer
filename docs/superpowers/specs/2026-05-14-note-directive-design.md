# 备注框 @/# 智能指令设计

- **日期**：2026-05-14
- **分支预期**：在新工作树上以 `feature/note-directive` 实现
- **关联模块**：`core/tools/match`、`core/data/usecase`、`core/behaviorui/sheet`、`feature/home`

---

## 1. 背景与目标

当前备注框已有"反向扫描"能力（`NoteMatcher`）：用户敲完备注后，系统会扫出备注里"包含"的活动/标签名或 keyword token，写入选中状态。这是"被动认识"的范式 —— 备注里出现什么就识别什么。

新需求引入一种**主动声明**范式：

- `@name` —— 表达"这条行为属于活动 `name`"。若已存在同名活动则选中，否则**立即创建并选中**。
- `#name` —— 表达"给这条行为打上标签 `name`"。若已存在则选中，否则**立即创建并选中**。
- `!@name` / `！@name`（CJK 全角同效，`#` 同理）—— **转义**：当前位置不识别，避免把邮箱、URL、文档片段等误当指令。

主动声明优先级高于反向扫描，并且仅在用户**点击"智能识别"按钮**时执行 —— 不进入自动 300ms 防抖路径，避免按键过程中创建半成品活动。

## 2. 用户故事

### 故事 1 —— 创建并选中新活动
- 用户输入 `@夜跑 沿江三公里 #健康`
- 点击「智能识别」
- 系统识别到 `@夜跑` 为新活动 → 静默创建 → 选中
- 识别 `#健康` 为新标签 → 静默创建 → 加入选中
- 备注变为 `夜跑 沿江三公里 健康`
- Toast：「已新增活动『夜跑』和 1 个标签」

### 故事 2 —— 多个 @ 仅选中最后一个
- 用户输入 `@跑步 @阅读`
- 点击「智能识别」
- 跑步、阅读若不存在均被创建
- 最终 selectedActivityId 指向**阅读**
- Toast：「已新增 2 个活动，已选中『阅读』」

### 故事 3 —— 邮箱转义
- 用户输入 `联系 sales!@example.com`
- 点击「智能识别」
- `!@example` 被识别为转义，本次跳过；备注保持 `联系 sales!@example.com` 原样

### 故事 4 —— 复用现有项
- 已存在活动 `跑步`
- 用户输入 `@跑步`
- 点击「智能识别」
- 不创建新活动，仅选中现有 `跑步`
- Toast：「已识别活动『跑步』」

## 3. 解析规则

### 3.1 触发条件

| 维度 | 规则 |
|---|---|
| 触发符 | `@` 表示活动指令（单选语义）；`#` 表示标签指令（多选语义） |
| **前置上下文** | 不限制 —— `@` / `#` 前可紧贴任意字符（含字母、数字、CJK） |
| **转义符** | 紧贴触发符前一个字符为 `!`（ASCII 半角）或 `！`（CJK 全角）时，强制跳过本次识别 |
| **后边界** | 命中以下任一字符即结束：空白（含全角空格 `　`）、换行 `\r\n`、ASCII 标点 `,.!?:;'"()[]{}<>/\\|`、CJK 标点 `，。、！？：；"" '' 「」《》（）【】〈〉`、下一个 `@` 或 `#`、字符串末尾 |
| **空 name** | 触发符直接遇到边界（如 `@ `、`@@`、字符串以 `@` 结尾）→ 整段忽略，不进入 directive 列表，不清除该触发符 |
| **长度上限** | name ≤ 32 字符，超长部分截断后仍记为 directive（防御性硬上限） |
| **大小写** | directive 中保留原样；查重时按 `lowercase` 比较 |

### 3.2 转义符细节

- 转义符 `!` / `！` 与触发符**必须紧贴**，中间不能有任何字符
- `xx !@y` —— `!` 与 `@` 之间有空格 → 转义不生效，`@y` 正常触发
- `!@y` 出现在字符串起始 → 转义生效（前置不限制）
- 转义对 `@` / `#` 同等适用，单独使用 `!` 或 `！` 不影响其余文本

### 3.3 cleanedNote 规则

| 情形 | 处理 |
|---|---|
| 正常被识别的 `@` / `#` | 仅删除该 `@` / `#` 单字符，保留 name 及周围所有内容 |
| 被转义的 `!@name` / `！#name` | **整段保留原样**，包括 `!` / `！` 与 `@` / `#` |
| 空 name 的 `@` / `#` | 保留原 `@` / `#` 字符不删除 |
| 其余文本 | 不变（空白、标点、换行原位保留） |

举例：
- 输入：`a!@b@c #d ！#e`
- directives：`[@"c", #"d"]`（`!@b` 被转义，`！#e` 被转义）
- cleanedNote：`a!@bc d ！#e`

### 3.4 解析输出契约

```kotlin
object NoteDirectiveParser {
    data class Directive(
        val symbol: Char,          // '@' 或 '#'
        val name: String,          // 原大小写，已 trim
        val range: IntRange,       // 在原 note 中的索引区间（含两端，含触发符）
    )

    data class ParseResult(
        val directives: List<Directive>,  // 按出现顺序
        val cleanedNote: String,
    )

    fun parse(note: String): ParseResult
}
```

## 4. 应用规则（UseCase）

### 4.1 接口契约

```kotlin
class ApplyNoteDirectivesUseCase @Inject constructor(
    private val addActivityUseCase: AddActivityUseCase,
    private val addTagUseCase: AddTagUseCase,
) {
    data class Outcome(
        val lastActivityId: Long?,
        val addedTagIds: Set<Long>,
        val createdActivityNames: List<String>,
        val createdTagNames: List<String>,
        val matchedActivityNames: List<String>,
        val matchedTagNames: List<String>,
    ) {
        companion object {
            val Empty = Outcome(null, emptySet(), emptyList(), emptyList(), emptyList(), emptyList())
        }
    }

    suspend operator fun invoke(
        directives: List<NoteDirectiveParser.Directive>,
        existingActivities: List<Activity>,
        existingTags: List<Tag>,
    ): Outcome
}
```

### 4.2 处理流程

1. 按出现顺序遍历 directives
2. `symbol == '@'`：
   - 在 `existingActivities` 里查找 `name.equals(directive.name, ignoreCase = true)` 且 `!isArchived` 的项
   - 命中 → 记录其 id 到候选活动列表
   - 未命中 → 调 `addActivityUseCase(name = directive.name.trim(), iconKey = null, color = null, groupId = null, keywords = null, tagIds = emptyList())`，得到新 id 加入候选
3. `symbol == '#'`：
   - 在 `existingTags` 里查找 `name.equals(directive.name, ignoreCase = true)` 且 `!isArchived` 的项
   - 命中 → 加入 addedTagIds
   - 未命中 → 调 `addTagUseCase(name = directive.name.trim(), color = null, iconKey = null, priority = 0, category = null, keywords = null, activityId = null)`，得到新 id 加入 addedTagIds
4. `lastActivityId = 候选活动列表.lastOrNull()`
5. 返回 Outcome

### 4.3 行为细节

- **归档项跳过命中**：归档活动/标签视为不存在，会被重新创建（避免静默"复活"归档项）
- **大小写不敏感查重**：`@StuDYing` 命中现有 `studying`
- **trim**：name 在解析器中已 trim；UseCase 不再二次处理
- **同批次去重**：同一次解析中出现两个相同 `@a` 时，第二个不再调用 addActivityUseCase，复用第一次的 id（维护本批次"已创建/已命中"内部缓存）
- **失败不阻断**：单个 directive 创建失败时记录日志后继续后续 directive 处理（防止部分失败导致整体回滚的 UX 困惑）

## 5. UI 集成

### 5.1 ViewModel

`HomeViewModel` 新增（注入 `ApplyNoteDirectivesUseCase`）：

```kotlin
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

suspend fun processNote(note: String): NoteProcessOutcome {
    val parsed = NoteDirectiveParser.parse(note)
    val directive = applyNoteDirectivesUseCase(
        parsed.directives,
        _activities.value,
        _allTags.value,
    )
    val scan = noteMatcher.scan(parsed.cleanedNote, _activities.value, _allTags.value)
    return NoteProcessOutcome(parsed.cleanedNote, directive, scan)
}
```

### 5.2 AddBehaviorState 扩展

```kotlin
internal data class NoteDirectiveApplyOutcome(
    val activityOverridden: Boolean,
    val tagsAdded: Int,
)

fun applyDirectiveOutcome(outcome: ApplyNoteDirectivesUseCase.Outcome): NoteDirectiveApplyOutcome {
    val activityOverridden = if (outcome.lastActivityId != null) {
        selectedActivityId = outcome.lastActivityId
        true
    } else false
    val before = selectedTagIds.size
    selectedTagIds = selectedTagIds + outcome.addedTagIds
    return NoteDirectiveApplyOutcome(activityOverridden, selectedTagIds.size - before)
}
```

### 5.3 "智能识别"按钮逻辑

`AddBehaviorSheetContent.onTopButton` 改为（注：需要 `rememberCoroutineScope`）：

```kotlin
onTopButton = {
    val raw = state.note
    if (raw.isBlank()) {
        Toast.makeText(context, "请输入备注后再识别", Toast.LENGTH_SHORT).show()
        return@NoteInputComponent
    }
    scope.launch {
        val processed = onProcessNote(raw)
        state.note = processed.cleanedNote
        val directiveOutcome = state.applyDirectiveOutcome(processed.directiveOutcome)
        val scanOutcome = state.applyNoteScan(processed.scanResult)
        Toast.makeText(
            context,
            buildFeedbackMessage(processed.directiveOutcome, directiveOutcome, scanOutcome),
            Toast.LENGTH_SHORT,
        ).show()
    }
}
```

### 5.4 回调签名变化

`AddBehaviorSheet` / `AddCurrentBehaviorSheet` / `AddTargetBehaviorSheet` / `AddBehaviorSheetContent` / `BehaviorSheetWrapper` 增加参数：

```kotlin
onProcessNote: suspend (String) -> NoteProcessOutcome = { NoteProcessOutcome.Empty },
```

提供默认 no-op，让未消费此能力的调用方（如 `BehaviorManagementScreen`）无需改动。

保留 `onMatchNote` 不变 —— 它继续服务 `NoteAutoMatchEffect`（自动 300ms 防抖，纯反向扫描，无副作用）。

实际接线点（grep 锁定）：

- `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt` — 新增 `onProcessNote = remember { { viewModel.processNote(it) } }`，传到 HomeScreen
- `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt` — 接收并透传到 router
- `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeSheetRouter.kt` — 3 处 sheet 调用透传
- `feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorManagementScreen.kt` — **不修改**，由默认 no-op 兜底；编辑历史行为不参与 directive 智能识别

### 5.5 反馈文案

`buildFeedbackMessage(directiveOutcome, applyOutcome, scanOutcome)` 按以下优先级生成 Toast（实现放在 sheet 内私有顶层函数）：

| 序号 | 触发条件 | Toast 文案 |
|---|---|---|
| 1 | 有新增（createdActivityNames 或 createdTagNames 任一非空） | `"已新增 ${N}个活动和${M}个标签"`，零项省略对应词与连接词；活动有 1 项时附 `『name』`，多项时仅报数量 |
| 2 | 无新增，但 directiveOutcome 命中了活动或标签 | `"已识别活动/标签『xx』"`（活动优先；多项报数量） |
| 3 | directive 全空，反向扫描有变化 | 沿用现有手动按钮反馈逻辑（`outcome.hasAnyChange` 等） |
| 4 | 完全无变化 | `"未识别到活动或标签"` |

第 2 节"用户故事"中的 Toast 是**示意**，与本节生成规则在精确格式上以本节为准。

## 6. 与现有自动扫描的关系

| 维度 | 自动扫描（300ms 防抖） | 智能识别按钮（新流程） |
|---|---|---|
| 触发 | 输入变化时 | 用户点击 |
| 是否走 directive 解析 | **否** | 是 |
| 创建活动/标签 | 不会 | 可能创建 |
| 写回选中规则 | activity 仅空填、tags union | activity **覆盖**、tags union |
| 文本修改 | 无 | 删除被识别的 `@` / `#` 单字符 |
| Toast | 静默 | 有 |

`NoteAutoMatchEffect` 与 `NoteMatcher.scan` 行为完全不变。

## 7. 文件清单

### 新增

- `core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt`
- `core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt`
- `core/data/src/main/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCase.kt`
- `core/data/src/test/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCaseTest.kt`

### 修改

| 文件 | 修改要点 |
|---|---|
| `feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt` | 注入 `ApplyNoteDirectivesUseCase`，新增 `processNote` |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt` | 增加 `onProcessNote` lambda，包装 `viewModel.processNote` |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt` | 增加 `onProcessNote` 参数，透传到 router |
| `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeSheetRouter.kt` | 3 处 sheet 调用透传 `onProcessNote` |
| `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt` | 新增 `applyDirectiveOutcome` 与 `NoteDirectiveApplyOutcome` |
| `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheet.kt` | 加 `onProcessNote` 参数（三个变体函数 + Wrapper），默认 no-op |
| `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt` | `SheetMainContent` 接 `onProcessNote`，`onTopButton` 重写为协程版 |

不修改：`feature/behavior_management/src/main/java/com/nltimer/feature/behavior_management/ui/BehaviorManagementScreen.kt` —— 通过默认 no-op 兜底。

## 8. 测试策略

### 8.1 NoteDirectiveParserTest（表驱动）

至少覆盖：

- 单 `@name` / 单 `#name`
- 多 directive 顺序保留
- 前置任意字符（字母、数字、CJK 紧贴）
- 各类后边界：空白、`\n`、`\r\n`、ASCII 标点、CJK 标点、下一个 `@` 或 `#`、EOF
- 空 name：`@`、`@ `、`@@x`、`#` 在末尾
- 转义：`!@x`、`！@x`、`!#x`、`！#x`、`xx!@y`
- 紧贴位置敏感性：`! @x`（中间有空格）→ `@x` 仍触发
- 长度上限 32（输入 33 字符的 name → 截断）
- cleanedNote 正确性逐项断言
- 大小写保留：`@StuDYing` 的 directive name 保留 `StuDYing`
- range 索引正确性（含全角字符）

### 8.2 ApplyNoteDirectivesUseCaseTest

- 已存在活动 → 仅返回 id，不调 addActivityUseCase
- 不存在活动 → 调 addActivityUseCase 一次，参数为 (name=trim, 其余 null/0)
- 多个 `@` → `lastActivityId` 为最后一个
- 多个 `#` 全部 union 到 addedTagIds
- 归档项跳过命中（含 isArchived=true 的同名项也会触发创建）
- 大小写不敏感命中
- 同批次去重：`@a @a` → 第二个不会再次创建

### 8.3 不写 UI 测试

与现状一致：sheet 与 ViewModel 改动通过手动验收。

## 9. 风险与权衡

| 风险 | 应对 |
|---|---|
| 邮箱、URL 中的 `@` 被误识别为指令 | 由用户用 `!@` / `！@` 转义；UX 通过 Toast 反馈给用户确认 |
| 一次输入创建大量活动/标签污染数据库 | 现有"撤销"路径在管理页删除；当前不引入"确认弹窗"以保持流畅；后续若投诉再加 |
| 单个 directive 创建失败导致后续中断 | UseCase 内部 try/catch 单 directive，记日志继续；Outcome 仅返回成功项 |
| `processNote` 是 suspend，按钮多次快速点击 | 用 `rememberCoroutineScope` + 内部布尔状态防抖；首版可不防抖（用户极少会快速连点） |

## 10. 不在本期实现的事项

- 直接输入框内带高亮（`@xxx` 染色显示）
- 自动补全下拉（输入 `@` 弹出活动列表）
- 与 AI Agent 工具系统的对接（虽然 Parser 是无副作用纯函数，未来 Agent 可独立复用，但本期不增加 ToolDefinition 实现）
- 撤销最近一次智能识别（需要快照系统，单独需求）
