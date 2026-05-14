# 备注框 @/# 智能指令实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为备注框「智能识别」按钮新增 `@name` / `#name` 主动指令，缺失即静默创建活动/标签并选中；`!`/`！` 前缀作为转义。

**架构：**
- 纯函数 `NoteDirectiveParser` 解析备注，输出 directive 列表与 `cleanedNote`
- `ApplyNoteDirectivesUseCase` 编排查重 + 调用 `AddActivityUseCase` / `AddTagUseCase` 创建
- `HomeViewModel.processNote` 串联 Parser → UseCase → 既有 `NoteMatcher`
- Sheet 层通过 `onProcessNote: suspend (String) -> NoteProcessOutcome` 回调消费，默认 no-op 兜底 `BehaviorManagementScreen`

**技术栈：** Kotlin · Hilt · Compose · Kotlin Coroutines · JUnit 4 · MockK · kotlinx-coroutines-test

**工作树：** `.worktrees/feature-note-directive`（分支 `feature/note-directive`，基于 dev-v5 @17eccf4）

**规格依据：** [`docs/superpowers/specs/2026-05-14-note-directive-design.md`](../specs/2026-05-14-note-directive-design.md)

---

## 文件结构

### 新建（4 个）

- `core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt`
  纯函数 `object`，无依赖；定义 `Directive` / `ParseResult` / `parse()`
- `core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt`
  表驱动 + 分块测试（共 ≥ 20 用例）
- `core/data/src/main/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCase.kt`
  Hilt UseCase；定义 `Outcome` / `Outcome.Empty`，依赖 `AddActivityUseCase` + `AddTagUseCase`
- `core/data/src/test/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCaseTest.kt`
  MockK + runTest

### 修改（7 个）

- `feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`
- `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`
- `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`
- `feature/home/src/main/java/com/nltimer/feature/home/ui/HomeSheetRouter.kt`
- `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt`
- `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheet.kt`
- `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt`

不修改：`feature/behavior_management/.../BehaviorManagementScreen.kt`（默认 no-op 兜底）。

---

## 任务 1：NoteDirectiveParser — 数据结构与基本解析

**文件：**
- 创建：`core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt`
- 测试：`core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt`

### - [ ] 步骤 1.1：编写第一批失败测试

写到 `core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt`：

```kotlin
package com.nltimer.core.tools.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDirectiveParserTest {

    @Test
    fun `single at directive captures name`() {
        val r = NoteDirectiveParser.parse("@跑步")
        assertEquals(1, r.directives.size)
        assertEquals('@', r.directives[0].symbol)
        assertEquals("跑步", r.directives[0].name)
    }

    @Test
    fun `single hash directive captures name`() {
        val r = NoteDirectiveParser.parse("#晨练")
        assertEquals(1, r.directives.size)
        assertEquals('#', r.directives[0].symbol)
        assertEquals("晨练", r.directives[0].name)
    }

    @Test
    fun `multiple directives preserve order`() {
        val r = NoteDirectiveParser.parse("@a @b #c")
        assertEquals(listOf('@', '@', '#'), r.directives.map { it.symbol })
        assertEquals(listOf("a", "b", "c"), r.directives.map { it.name })
    }

    @Test
    fun `lone at sign at end is ignored`() {
        val r = NoteDirectiveParser.parse("note @")
        assertTrue(r.directives.isEmpty())
    }

    @Test
    fun `at followed by space is ignored`() {
        val r = NoteDirectiveParser.parse("note @ tail")
        assertTrue(r.directives.isEmpty())
    }

    @Test
    fun `at followed by at is empty name then captures next`() {
        val r = NoteDirectiveParser.parse("@@x")
        assertEquals(1, r.directives.size)
        assertEquals("x", r.directives[0].name)
    }

    @Test
    fun `case preserved in directive name`() {
        val r = NoteDirectiveParser.parse("@StuDYing")
        assertEquals("StuDYing", r.directives[0].name)
    }
}
```

### - [ ] 步骤 1.2：运行测试验证失败

```bash
cd .worktrees/feature-note-directive
./gradlew :core:tools:testDebugUnitTest --tests "com.nltimer.core.tools.match.NoteDirectiveParserTest"
```

预期：FAIL，"Unresolved reference: NoteDirectiveParser"。

### - [ ] 步骤 1.3：创建解析器最小实现

写到 `core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt`：

```kotlin
package com.nltimer.core.tools.match

/**
 * 备注主动指令解析器：在备注里识别 `@name` / `#name` 形式的"主动声明"。
 *
 * 与同 package 下 [NoteMatcher]（反向扫描"备注包含哪些已存在的活动/标签"）的关系：
 * - 本类做"用户主动声明" → 上游 UseCase 决定是否创建并选中
 * - 两者由 ViewModel 编排串联，互不干扰
 *
 * 设计依据：`docs/superpowers/specs/2026-05-14-note-directive-design.md`
 */
object NoteDirectiveParser {

    private const val MAX_NAME_LENGTH = 32

    data class Directive(
        val symbol: Char,
        val name: String,
        val range: IntRange,
    )

    data class ParseResult(
        val directives: List<Directive>,
        val cleanedNote: String,
    )

    fun parse(note: String): ParseResult {
        if (note.isEmpty()) return ParseResult(emptyList(), note)
        val directives = mutableListOf<Directive>()
        val cleaned = StringBuilder(note.length)
        var i = 0
        while (i < note.length) {
            val ch = note[i]
            if (ch == '@' || ch == '#') {
                val nameEnd = scanNameEnd(note, i + 1)
                val rawName = note.substring(i + 1, nameEnd)
                if (rawName.isEmpty()) {
                    cleaned.append(ch)
                    i++
                    continue
                }
                val name = if (rawName.length > MAX_NAME_LENGTH) {
                    rawName.substring(0, MAX_NAME_LENGTH)
                } else rawName
                directives += Directive(ch, name, i..(nameEnd - 1))
                cleaned.append(rawName)
                i = nameEnd
            } else {
                cleaned.append(ch)
                i++
            }
        }
        return ParseResult(directives, cleaned.toString())
    }

    /** 从 [start] 开始向后扫描，返回 name 结束位置（开区间右端） */
    private fun scanNameEnd(note: String, start: Int): Int {
        var j = start
        while (j < note.length) {
            val c = note[j]
            if (isBoundary(c)) break
            j++
        }
        return j
    }

    private fun isBoundary(c: Char): Boolean {
        if (c.isWhitespace()) return true
        if (c == '@' || c == '#') return true
        if (c in ",.!?:;'\"()[]{}<>/\\|") return true
        if (c in "，。、！？：；“”‘’「」《》（）【】〈〉") return true
        return false
    }
}
```

### - [ ] 步骤 1.4：运行测试验证通过

```bash
./gradlew :core:tools:testDebugUnitTest --tests "com.nltimer.core.tools.match.NoteDirectiveParserTest"
```

预期：7 tests PASSED。

### - [ ] 步骤 1.5：Commit

```bash
git add core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt \
        core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt
git commit -m "$(cat <<'EOF'
feat(备注框): 引入 NoteDirectiveParser 解析 @/# 主动指令

支持单/多 directive、空 name 忽略、大小写保留、ASCII/CJK 标点边界。
转义符与长度上限在后续 task 补全。

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 2：NoteDirectiveParser — 转义符、长度上限与 cleanedNote 精化

**文件：**
- 修改：`core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt`
- 修改：`core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt`

### - [ ] 步骤 2.1：追加失败测试

在 `NoteDirectiveParserTest.kt` 的类内追加：

```kotlin
    // ─── cleanedNote ───

    @Test
    fun `cleanedNote removes recognised at and hash only`() {
        val r = NoteDirectiveParser.parse("a @b c #d e")
        assertEquals("a b c d e", r.cleanedNote)
    }

    @Test
    fun `cleanedNote preserves lone at when no name follows`() {
        val r = NoteDirectiveParser.parse("price @ 9.9")
        assertEquals("price @ 9.9", r.cleanedNote)
    }

    @Test
    fun `cleanedNote preserves surrounding whitespace and punctuation`() {
        val r = NoteDirectiveParser.parse("hello, @world!")
        assertEquals("hello, world!", r.cleanedNote)
        assertEquals("world", r.directives[0].name)
    }

    // ─── escape ! / ！ ───

    @Test
    fun `ascii bang escapes at directive`() {
        val r = NoteDirectiveParser.parse("sales!@example.com")
        assertTrue(r.directives.isEmpty())
        assertEquals("sales!@example.com", r.cleanedNote)
    }

    @Test
    fun `cjk bang escapes hash directive`() {
        val r = NoteDirectiveParser.parse("note ！#tag")
        assertTrue(r.directives.isEmpty())
        assertEquals("note ！#tag", r.cleanedNote)
    }

    @Test
    fun `bang separated by space does not escape`() {
        val r = NoteDirectiveParser.parse("! @y")
        assertEquals(1, r.directives.size)
        assertEquals("y", r.directives[0].name)
    }

    @Test
    fun `escape at start of string applies`() {
        val r = NoteDirectiveParser.parse("!@y rest")
        assertTrue(r.directives.isEmpty())
        assertEquals("!@y rest", r.cleanedNote)
    }

    @Test
    fun `mixed escaped and non-escaped`() {
        val r = NoteDirectiveParser.parse("a!@b@c #d ！#e")
        assertEquals(listOf("c", "d"), r.directives.map { it.name })
        assertEquals("a!@bc d ！#e", r.cleanedNote)
    }

    // ─── boundaries / length ───

    @Test
    fun `cjk punctuation ends name`() {
        val r = NoteDirectiveParser.parse("@跑步，今天")
        assertEquals("跑步", r.directives[0].name)
    }

    @Test
    fun `name exceeding 32 chars is truncated`() {
        val longName = "a".repeat(40)
        val r = NoteDirectiveParser.parse("@$longName tail")
        assertEquals(32, r.directives[0].name.length)
    }

    @Test
    fun `newline ends name`() {
        val r = NoteDirectiveParser.parse("@aaa\nrest")
        assertEquals("aaa", r.directives[0].name)
    }

    @Test
    fun `range covers symbol through end of name inclusive`() {
        val r = NoteDirectiveParser.parse("x @ab y")
        assertEquals(2..4, r.directives[0].range)
    }
```

### - [ ] 步骤 2.2：运行测试验证失败

```bash
./gradlew :core:tools:testDebugUnitTest --tests "com.nltimer.core.tools.match.NoteDirectiveParserTest"
```

预期：转义相关用例 FAIL（5 个）；其他可能通过（取决于步骤 1.3 的行为）。

### - [ ] 步骤 2.3：在 parse 函数中插入转义检测

把 `NoteDirectiveParser.kt` 中 `parse` 函数体替换为：

```kotlin
    fun parse(note: String): ParseResult {
        if (note.isEmpty()) return ParseResult(emptyList(), note)
        val directives = mutableListOf<Directive>()
        val cleaned = StringBuilder(note.length)
        var i = 0
        while (i < note.length) {
            val ch = note[i]
            if (ch == '@' || ch == '#') {
                val escaped = i > 0 && (note[i - 1] == '!' || note[i - 1] == '！')
                val nameEnd = scanNameEnd(note, i + 1)
                val rawName = note.substring(i + 1, nameEnd)
                if (escaped) {
                    cleaned.append(ch)
                    cleaned.append(rawName)
                    i = nameEnd
                    continue
                }
                if (rawName.isEmpty()) {
                    cleaned.append(ch)
                    i++
                    continue
                }
                val name = if (rawName.length > MAX_NAME_LENGTH) {
                    rawName.substring(0, MAX_NAME_LENGTH)
                } else rawName
                directives += Directive(ch, name, i..(nameEnd - 1))
                cleaned.append(rawName)
                i = nameEnd
            } else {
                cleaned.append(ch)
                i++
            }
        }
        return ParseResult(directives, cleaned.toString())
    }
```

### - [ ] 步骤 2.4：运行测试验证全部通过

```bash
./gradlew :core:tools:testDebugUnitTest --tests "com.nltimer.core.tools.match.NoteDirectiveParserTest"
```

预期：19 tests PASSED, 0 failed。

### - [ ] 步骤 2.5：Commit

```bash
git add core/tools/src/main/java/com/nltimer/core/tools/match/NoteDirectiveParser.kt \
        core/tools/src/test/java/com/nltimer/core/tools/match/NoteDirectiveParserTest.kt
git commit -m "$(cat <<'EOF'
feat(备注框): NoteDirectiveParser 支持 !/！ 转义与边界精化

- !/！ 紧贴 @/# 时跳过识别，原段保留在 cleanedNote
- 中英标点、空白、下一个触发符均作为 name 后边界
- name 上限 32 字符截断

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 3：ApplyNoteDirectivesUseCase — 查重/创建与同批次去重

**文件：**
- 创建：`core/data/src/main/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCase.kt`
- 创建：`core/data/src/test/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCaseTest.kt`

### - [ ] 步骤 3.1：编写失败测试

写到 `core/data/src/test/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCaseTest.kt`：

```kotlin
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import com.nltimer.core.tools.match.NoteDirectiveParser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApplyNoteDirectivesUseCaseTest {

    private lateinit var addActivityUseCase: AddActivityUseCase
    private lateinit var addTagUseCase: AddTagUseCase
    private lateinit var useCase: ApplyNoteDirectivesUseCase

    @Before
    fun setup() {
        addActivityUseCase = mockk()
        addTagUseCase = mockk()
        useCase = ApplyNoteDirectivesUseCase(addActivityUseCase, addTagUseCase)
    }

    private fun activity(id: Long, name: String, archived: Boolean = false) =
        Activity(id = id, name = name, isArchived = archived)

    private fun tag(id: Long, name: String, archived: Boolean = false) = Tag(
        id = id, name = name, color = null, iconKey = null, category = null,
        priority = 0, usageCount = 0, sortOrder = 0, keywords = null, isArchived = archived,
    )

    private fun atDir(name: String) = NoteDirectiveParser.Directive('@', name, IntRange.EMPTY)
    private fun hashDir(name: String) = NoteDirectiveParser.Directive('#', name, IntRange.EMPTY)

    @Test
    fun `empty directives returns Empty outcome`() = runTest {
        val out = useCase(emptyList(), emptyList(), emptyList())
        assertNull(out.lastActivityId)
        assertTrue(out.addedTagIds.isEmpty())
        assertTrue(out.createdActivityNames.isEmpty())
        assertTrue(out.createdTagNames.isEmpty())
    }

    @Test
    fun `existing activity is matched without creating`() = runTest {
        val activities = listOf(activity(7L, "跑步"))
        val out = useCase(listOf(atDir("跑步")), activities, emptyList())
        assertEquals(7L, out.lastActivityId)
        assertTrue(out.createdActivityNames.isEmpty())
        coVerify(exactly = 0) { addActivityUseCase(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `unknown activity is created with default attrs`() = runTest {
        coEvery {
            addActivityUseCase("新跑", null, null, null, null, emptyList())
        } returns 42L
        val out = useCase(listOf(atDir("新跑")), emptyList(), emptyList())
        assertEquals(42L, out.lastActivityId)
        assertEquals(listOf("新跑"), out.createdActivityNames)
        coVerify(exactly = 1) {
            addActivityUseCase("新跑", null, null, null, null, emptyList())
        }
    }

    @Test
    fun `multiple at directives lastActivityId is last`() = runTest {
        coEvery { addActivityUseCase("a", null, null, null, null, emptyList()) } returns 1L
        coEvery { addActivityUseCase("b", null, null, null, null, emptyList()) } returns 2L
        val out = useCase(listOf(atDir("a"), atDir("b")), emptyList(), emptyList())
        assertEquals(2L, out.lastActivityId)
        assertEquals(listOf("a", "b"), out.createdActivityNames)
    }

    @Test
    fun `same name in batch is not created twice`() = runTest {
        coEvery { addActivityUseCase("a", null, null, null, null, emptyList()) } returns 1L
        val out = useCase(listOf(atDir("a"), atDir("a")), emptyList(), emptyList())
        assertEquals(1L, out.lastActivityId)
        coVerify(exactly = 1) {
            addActivityUseCase("a", null, null, null, null, emptyList())
        }
    }

    @Test
    fun `case insensitive match against existing`() = runTest {
        val activities = listOf(activity(9L, "studying"))
        val out = useCase(listOf(atDir("StuDYing")), activities, emptyList())
        assertEquals(9L, out.lastActivityId)
        coVerify(exactly = 0) { addActivityUseCase(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `archived existing skips match and creates new`() = runTest {
        coEvery { addActivityUseCase("zzz", null, null, null, null, emptyList()) } returns 99L
        val activities = listOf(activity(1L, "zzz", archived = true))
        val out = useCase(listOf(atDir("zzz")), activities, emptyList())
        assertEquals(99L, out.lastActivityId)
        assertEquals(listOf("zzz"), out.createdActivityNames)
    }

    @Test
    fun `hash directives union into addedTagIds`() = runTest {
        coEvery { addTagUseCase("t1", null, null, 0, null, null, null) } returns 11L
        coEvery { addTagUseCase("t2", null, null, 0, null, null, null) } returns 12L
        val out = useCase(listOf(hashDir("t1"), hashDir("t2")), emptyList(), emptyList())
        assertEquals(setOf(11L, 12L), out.addedTagIds)
        assertNull(out.lastActivityId)
    }

    @Test
    fun `existing tag is matched without creating`() = runTest {
        val tags = listOf(tag(5L, "重要"))
        val out = useCase(listOf(hashDir("重要")), emptyList(), tags)
        assertEquals(setOf(5L), out.addedTagIds)
        coVerify(exactly = 0) { addTagUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `mixed at and hash`() = runTest {
        coEvery { addActivityUseCase("跑", null, null, null, null, emptyList()) } returns 7L
        coEvery { addTagUseCase("健康", null, null, 0, null, null, null) } returns 8L
        val out = useCase(listOf(atDir("跑"), hashDir("健康")), emptyList(), emptyList())
        assertEquals(7L, out.lastActivityId)
        assertEquals(setOf(8L), out.addedTagIds)
    }
}
```

### - [ ] 步骤 3.2：运行测试验证失败

```bash
./gradlew :core:data:testDebugUnitTest --tests "com.nltimer.core.data.usecase.ApplyNoteDirectivesUseCaseTest"
```

预期：FAIL，"Unresolved reference: ApplyNoteDirectivesUseCase"。

### - [ ] 步骤 3.3：编写 UseCase 实现

写到 `core/data/src/main/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCase.kt`：

```kotlin
package com.nltimer.core.data.usecase

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.Tag
import com.nltimer.core.tools.match.NoteDirectiveParser
import javax.inject.Inject

/**
 * 应用 @/# 主动指令：
 * - 命中现有非归档同名活动/标签 → 仅返回其 id
 * - 未命中 → 调 [AddActivityUseCase] / [AddTagUseCase] 静默默认创建
 * - 多个 @：按顺序处理，lastActivityId 为最后一个候选
 * - 多个 #：全部 id union 到 addedTagIds
 * - 同批次中相同 name 仅创建一次
 *
 * 设计依据：docs/superpowers/specs/2026-05-14-note-directive-design.md §4
 */
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
    ): Outcome {
        if (directives.isEmpty()) return Outcome.Empty

        val batchActivities = mutableMapOf<String, Long>()
        val batchTags = mutableMapOf<String, Long>()
        val activityIdsInOrder = mutableListOf<Long>()
        val tagIdsCollected = linkedSetOf<Long>()
        val createdActivityNames = mutableListOf<String>()
        val createdTagNames = mutableListOf<String>()
        val matchedActivityNames = mutableListOf<String>()
        val matchedTagNames = mutableListOf<String>()

        for (d in directives) {
            val key = d.name.lowercase()
            when (d.symbol) {
                '@' -> {
                    val cached = batchActivities[key]
                    if (cached != null) {
                        activityIdsInOrder += cached
                        continue
                    }
                    val existing = existingActivities.firstOrNull {
                        !it.isArchived && it.name.equals(d.name, ignoreCase = true)
                    }
                    val id = if (existing != null) {
                        matchedActivityNames += existing.name
                        existing.id
                    } else {
                        val newId = runCatching {
                            addActivityUseCase(d.name, null, null, null, null, emptyList())
                        }.getOrNull() ?: continue
                        createdActivityNames += d.name
                        newId
                    }
                    batchActivities[key] = id
                    activityIdsInOrder += id
                }
                '#' -> {
                    val cached = batchTags[key]
                    if (cached != null) {
                        tagIdsCollected += cached
                        continue
                    }
                    val existing = existingTags.firstOrNull {
                        !it.isArchived && it.name.equals(d.name, ignoreCase = true)
                    }
                    val id = if (existing != null) {
                        matchedTagNames += existing.name
                        existing.id
                    } else {
                        val newId = runCatching {
                            addTagUseCase(d.name, null, null, 0, null, null, null)
                        }.getOrNull() ?: continue
                        createdTagNames += d.name
                        newId
                    }
                    batchTags[key] = id
                    tagIdsCollected += id
                }
            }
        }

        return Outcome(
            lastActivityId = activityIdsInOrder.lastOrNull(),
            addedTagIds = tagIdsCollected.toSet(),
            createdActivityNames = createdActivityNames,
            createdTagNames = createdTagNames,
            matchedActivityNames = matchedActivityNames,
            matchedTagNames = matchedTagNames,
        )
    }
}
```

### - [ ] 步骤 3.4：运行测试验证通过

```bash
./gradlew :core:data:testDebugUnitTest --tests "com.nltimer.core.data.usecase.ApplyNoteDirectivesUseCaseTest"
```

预期：10 tests PASSED。

### - [ ] 步骤 3.5：Commit

```bash
git add core/data/src/main/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCase.kt \
        core/data/src/test/java/com/nltimer/core/data/usecase/ApplyNoteDirectivesUseCaseTest.kt
git commit -m "$(cat <<'EOF'
feat(备注框): 新增 ApplyNoteDirectivesUseCase 编排查重与静默创建

- 命中非归档同名 → 复用 id；归档项跳过命中重新创建
- 大小写不敏感 + 同批次 name 去重
- 多 @ 取 lastActivityId 为最后一个，# 全部 union 到 addedTagIds
- 单个 directive 创建失败 try/catch 不中断后续

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 4：AddBehaviorState.applyDirectiveOutcome

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt`

无既有 sheet 单测目录，按项目现状不为此新增 UI 测试（参见 spec §8.3）。本任务通过编译 + 后续手动验收覆盖。

### - [ ] 步骤 4.1：在文件末尾追加 NoteDirectiveApplyOutcome

打开 `core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt`，在 `NoteScanApplyOutcome` 数据类**之后**追加：

```kotlin

/**
 * `applyDirectiveOutcome` 的副作用摘要，用于上层 Toast 反馈。
 *
 * @property activityOverridden directive 是否覆盖了 selectedActivityId
 * @property tagsAdded 本次新增到选中集合的标签数（去重后）
 */
internal data class NoteDirectiveApplyOutcome(
    val activityOverridden: Boolean,
    val tagsAdded: Int,
)
```

### - [ ] 步骤 4.2：在 AddBehaviorState 类内追加方法

在 `AddBehaviorState` 类内 `applyNoteScan` 函数**之后**、类闭合 `}` 之前追加：

```kotlin
    /**
     * 把 @/# directive 结果合并到选中状态。与 [applyNoteScan] 的差异：
     * 这里 activity 会**覆盖**已选项（@ 表达用户明确意图）；tags 仍 union。
     */
    fun applyDirectiveOutcome(
        outcome: com.nltimer.core.data.usecase.ApplyNoteDirectivesUseCase.Outcome,
    ): NoteDirectiveApplyOutcome {
        val activityOverridden = if (outcome.lastActivityId != null) {
            selectedActivityId = outcome.lastActivityId
            true
        } else false
        val before = selectedTagIds.size
        selectedTagIds = selectedTagIds + outcome.addedTagIds
        return NoteDirectiveApplyOutcome(
            activityOverridden = activityOverridden,
            tagsAdded = selectedTagIds.size - before,
        )
    }
```

### - [ ] 步骤 4.3：编译验证

```bash
./gradlew :core:behaviorui:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

### - [ ] 步骤 4.4：Commit

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorState.kt
git commit -m "$(cat <<'EOF'
feat(备注框): AddBehaviorState 新增 applyDirectiveOutcome

与 applyNoteScan 的差异：directive 表达用户明确意图，
activity 直接覆盖现有选中；tags 仍按 union 合并。

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 5：HomeViewModel.processNote 串联编排

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt`

### - [ ] 步骤 5.1：添加 import 与构造器注入

在 `HomeViewModel.kt` 文件 import 区添加：

```kotlin
import com.nltimer.core.data.usecase.ApplyNoteDirectivesUseCase
import com.nltimer.core.tools.match.NoteDirectiveParser
```

在构造器参数列表（其他 UseCase 之后、`clockService` 之前）插入：

```kotlin
    private val applyNoteDirectivesUseCase: ApplyNoteDirectivesUseCase,
```

### - [ ] 步骤 5.2：在 `matchNoteFromText` 函数之后追加 NoteProcessOutcome 与 processNote

在 `matchNoteFromText` 函数（约 300 行附近）**结束后**添加：

```kotlin

    /**
     * `processNote` 输出包：cleanedNote + directive 处理结果 + 反向扫描结果。
     */
    data class NoteProcessOutcome(
        val cleanedNote: String,
        val directiveOutcome: ApplyNoteDirectivesUseCase.Outcome,
        val scanResult: com.nltimer.core.tools.match.NoteScanResult,
    ) {
        companion object {
            val Empty = NoteProcessOutcome(
                cleanedNote = "",
                directiveOutcome = ApplyNoteDirectivesUseCase.Outcome.Empty,
                scanResult = com.nltimer.core.tools.match.NoteScanResult(null, emptySet()),
            )
        }
    }

    /**
     * 智能识别按钮的入口：解析 @/# directive → 创建/复用 → 反向扫描备注，
     * 由 UI 层接管把结果合并到 sheet 状态。
     */
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

### - [ ] 步骤 5.3：编译验证

```bash
./gradlew :feature:home:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。若 Hilt 报缺少依赖，确认 `core/data` 已在 `feature/home` 的依赖里（项目惯例已有）。

### - [ ] 步骤 5.4：Commit

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/viewmodel/HomeViewModel.kt
git commit -m "$(cat <<'EOF'
feat(主页): HomeViewModel.processNote 串联 directive 与反向扫描

注入 ApplyNoteDirectivesUseCase，新增 NoteProcessOutcome
作为智能识别按钮的统一出口。

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 6：Sheet 链增加 onProcessNote 参数（默认 no-op）

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheet.kt`
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt`

直接为函数签名加参数；默认值保证未消费方（如 `BehaviorManagementScreen`）零改动。

### - [ ] 步骤 6.1：定义类型别名以避免回调签名展开过长

在 `AddBehaviorSheet.kt` 的 imports 之后、`private const val ScrimAlpha` 之前追加：

```kotlin
import com.nltimer.feature.home.viewmodel.HomeViewModel.NoteProcessOutcome

/** Sheet 透传给 ViewModel 的"智能识别"统一回调；默认 no-op 兜底无 directive 流的页面。 */
typealias OnProcessNote = suspend (note: String) -> NoteProcessOutcome
```

> ⚠️ 此 typealias 引入了对 `feature/home` 的依赖。若 `core/behaviorui` 不允许依赖 `feature/home`（按 NLtimer 分层约定通常不允许），改用以下两种方式之一：
> 1. 把 `NoteProcessOutcome` 移到 `core/data` 的 `model/` 下（推荐）
> 2. Sheet 层定义本地等价数据类，由 ViewModel 适配
>
> 在步骤 6.1 开始前先执行 `cat core/behaviorui/build.gradle.kts | grep -E 'implementation|projects'` 确认依赖图。如发现违规，改走方式 1：把 `NoteProcessOutcome` 与 `Outcome.Empty` 一起放到 `core/data/src/main/java/com/nltimer/core/data/model/NoteProcessOutcome.kt`，所有引用相应修改。

### - [ ] 步骤 6.2：在 4 个 sheet 公共变体与 Wrapper 增加 onProcessNote 参数

对 `AddBehaviorSheet.kt` 中以下函数的参数列表，在 `onMatchNote = ...` 那一行**之前**插入新参数：

- `AddBehaviorSheet` (`@Composable` 主入口)
- `AddCurrentBehaviorSheet`
- `AddTargetBehaviorSheet`
- `BehaviorSheetWrapper` (`@Composable private`)

参数：

```kotlin
    onProcessNote: OnProcessNote = { NoteProcessOutcome.Empty },
```

每个公共变体的 `BehaviorSheetWrapper(...)` 调用里相应加一行 `onProcessNote = onProcessNote,`。

`BehaviorSheetWrapper` 内部对 `AddBehaviorSheetContent(...)` 的调用同样加一行 `onProcessNote = onProcessNote,`。

### - [ ] 步骤 6.3：在 AddBehaviorSheetContent.kt 同步签名

在 `AddBehaviorSheetContent` 与 `SheetMainContent` 的参数列表里增加 `onProcessNote: OnProcessNote = { NoteProcessOutcome.Empty }`（前者）/ `onProcessNote: OnProcessNote`（后者）。

`AddBehaviorSheetContent` 内部对 `SheetMainContent(...)` 的调用增加 `onProcessNote = onProcessNote,`。

### - [ ] 步骤 6.4：编译验证

```bash
./gradlew :core:behaviorui:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

### - [ ] 步骤 6.5：Commit

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheet.kt \
        core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt
git commit -m "$(cat <<'EOF'
feat(备注框): Sheet 链增加 onProcessNote 回调与 OnProcessNote 类型别名

四个公共变体 + Wrapper + SheetMainContent 全部加默认 no-op 参数；
BehaviorManagementScreen 无需改动即可兜底。

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 7：onTopButton 协程化 + buildFeedbackMessage

**文件：**
- 修改：`core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt`

### - [ ] 步骤 7.1：在 imports 添加

```kotlin
import androidx.compose.runtime.rememberCoroutineScope
import com.nltimer.core.data.usecase.ApplyNoteDirectivesUseCase
import kotlinx.coroutines.launch
```

### - [ ] 步骤 7.2：在 SheetMainContent 内创建 scope

在 `SheetMainContent` 函数体内、`val context = LocalContext.current` 之**后**追加：

```kotlin
    val scope = rememberCoroutineScope()
```

### - [ ] 步骤 7.3：替换 onTopButton 实现

把 `NoteInputComponent(...)` 调用中现有的 `onTopButton = { ... }` lambda 整体替换为：

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
                        val directiveApply = state.applyDirectiveOutcome(processed.directiveOutcome)
                        val scanApply = state.applyNoteScan(processed.scanResult)
                        Toast.makeText(
                            context,
                            buildFeedbackMessage(processed.directiveOutcome, directiveApply, scanApply),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
```

### - [ ] 步骤 7.4：在文件末尾追加 buildFeedbackMessage

在 `AddBehaviorSheetContent.kt` 文件**最末**（最后一个 `private const` 之后）追加私有顶层函数：

```kotlin

/**
 * 智能识别按钮按下后的 Toast 文案生成。
 *
 * 优先级：新增 > 命中现有 > 反向扫描有变化 > 完全无识别。
 * 单项时附名字，多项时只报数量。
 */
private fun buildFeedbackMessage(
    directiveOutcome: ApplyNoteDirectivesUseCase.Outcome,
    directiveApply: NoteDirectiveApplyOutcome,
    scanApply: NoteScanApplyOutcome,
): String {
    val createdActivities = directiveOutcome.createdActivityNames
    val createdTags = directiveOutcome.createdTagNames
    if (createdActivities.isNotEmpty() || createdTags.isNotEmpty()) {
        return buildString {
            append("已新增")
            if (createdActivities.isNotEmpty()) {
                if (createdActivities.size == 1) append("活动『${createdActivities.first()}』")
                else append("${createdActivities.size}个活动")
            }
            if (createdActivities.isNotEmpty() && createdTags.isNotEmpty()) append("和")
            if (createdTags.isNotEmpty()) {
                if (createdTags.size == 1) append("标签『${createdTags.first()}』")
                else append("${createdTags.size}个标签")
            }
        }
    }
    val matchedActivities = directiveOutcome.matchedActivityNames
    val matchedTags = directiveOutcome.matchedTagNames
    if (matchedActivities.isNotEmpty() || matchedTags.isNotEmpty()) {
        return buildString {
            append("已识别")
            if (matchedActivities.isNotEmpty()) {
                if (matchedActivities.size == 1) append("活动『${matchedActivities.first()}』")
                else append("${matchedActivities.size}个活动")
            }
            if (matchedActivities.isNotEmpty() && matchedTags.isNotEmpty()) append("和")
            if (matchedTags.isNotEmpty()) {
                if (matchedTags.size == 1) append("标签『${matchedTags.first()}』")
                else append("${matchedTags.size}个标签")
            }
        }
    }
    return when {
        scanApply.hasAnyChange -> buildString {
            append("已识别")
            if (scanApply.activityAdded) append("活动")
            if (scanApply.activityAdded && scanApply.tagsAdded > 0) append("和")
            if (scanApply.tagsAdded > 0) append("${scanApply.tagsAdded}个标签")
        }
        else -> "未识别到活动或标签"
    }
}
```

### - [ ] 步骤 7.5：编译验证

```bash
./gradlew :core:behaviorui:compileDebugKotlin
```

预期：BUILD SUCCESSFUL。

### - [ ] 步骤 7.6：Commit

```bash
git add core/behaviorui/src/main/java/com/nltimer/core/behaviorui/sheet/AddBehaviorSheetContent.kt
git commit -m "$(cat <<'EOF'
feat(备注框): 智能识别按钮接入 directive 流程并生成多档 Toast

onTopButton 改为协程化：调用 onProcessNote 取得 cleanedNote、
directive 结果与反向扫描结果后依次应用到 sheet 状态；
新增 buildFeedbackMessage 按新增 > 命中 > 扫描 > 兜底优先级生成文案。

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 8：HomeRoute / HomeScreen / HomeSheetRouter 接线

**文件：**
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt`
- 修改：`feature/home/src/main/java/com/nltimer/feature/home/ui/HomeSheetRouter.kt`

### - [ ] 步骤 8.1：HomeRoute 增加 onProcessNote lambda

打开 `HomeRoute.kt`，在 `val onMatchNote = remember(viewModel) { ... }` 块**之后**追加：

```kotlin
    val onProcessNote = remember(viewModel) {
        { note: String -> viewModel.processNote(note) }
    }
```

注意：`viewModel::processNote` 是 suspend，`remember` 持有的是 lambda；调用方按 suspend 签名消费。

在 `HomeScreen(...)` 调用里添加：

```kotlin
        onProcessNote = onProcessNote,
```

### - [ ] 步骤 8.2：HomeScreen 增加参数并透传

打开 `HomeScreen.kt`，在 `HomeScreen` 函数签名里增加：

```kotlin
    onProcessNote: suspend (String) -> com.nltimer.feature.home.viewmodel.HomeViewModel.NoteProcessOutcome,
```

并在调用 `HomeSheetRouter` 的位置加 `onProcessNote = onProcessNote,`。

### - [ ] 步骤 8.3：HomeSheetRouter 三处 sheet 调用透传

打开 `HomeSheetRouter.kt`，给 `HomeSheetRouter` 函数签名增加同样的 `onProcessNote` 参数。

在三处 sheet 调用（`AddBehaviorSheet` / `AddCurrentBehaviorSheet` / `AddTargetBehaviorSheet`）每个的参数块里、`onMatchNote = onMatchNote,` 这一行**之后**加：

```kotlin
            onProcessNote = onProcessNote,
```

### - [ ] 步骤 8.4：全模块编译 + 单测

```bash
./gradlew :feature:home:compileDebugKotlin
./gradlew :core:tools:testDebugUnitTest :core:data:testDebugUnitTest
```

预期：编译 BUILD SUCCESSFUL；单测全过（NoteDirectiveParserTest 19、ApplyNoteDirectivesUseCaseTest 10，加上原有测试不退化）。

### - [ ] 步骤 8.5：Commit

```bash
git add feature/home/src/main/java/com/nltimer/feature/home/ui/HomeRoute.kt \
        feature/home/src/main/java/com/nltimer/feature/home/ui/HomeScreen.kt \
        feature/home/src/main/java/com/nltimer/feature/home/ui/HomeSheetRouter.kt
git commit -m "$(cat <<'EOF'
feat(主页): HomeRoute/Screen/Router 透传 onProcessNote 至 sheet

将 viewModel.processNote 包成 suspend lambda 经 HomeScreen
→ HomeSheetRouter → 三种 sheet 入口，激活智能识别按钮的 directive 流程。

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
EOF
)"
```

---

## 任务 9：全量构建 + 手动验收

### - [ ] 步骤 9.1：Debug APK 构建

```bash
./gradlew :app:assembleDebug
```

预期：BUILD SUCCESSFUL。若 ABI 资源等待时间长，可加 `--no-daemon` 缓解。

### - [ ] 步骤 9.2：安装到设备/模拟器

```bash
./gradlew :app:installDebug
```

或将 `app/build/outputs/apk/debug/app-debug.apk` 推到设备并安装。

### - [ ] 步骤 9.3：人工验收清单（每条勾选）

打开主页 → 点击"+"或长按格子打开 `AddBehaviorSheet`，在备注框分别输入并点"智能识别"：

- [ ] `@夜跑` →（夜跑不存在）创建活动并选中；备注变 `夜跑`；Toast 含"新增活动『夜跑』"
- [ ] 重复 `@夜跑` →（已存在）仅选中，不再新建；Toast 含"识别活动『夜跑』"
- [ ] `@夜跑 @阅读` → 两者都创建，最终 selectedActivityId 指向"阅读"
- [ ] `#健康 #专注` → 两个标签都创建并加入选中
- [ ] `@跑步 沿江 #健康` → 复合：活动+标签同时生效；备注变 `跑步 沿江 健康`
- [ ] `联系 sales!@example.com` → 不触发任何识别；备注不变；Toast "未识别到活动或标签"
- [ ] `note ！#tag` → 同上，不识别；备注不变
- [ ] 已选活动 `工作` 时输入 `@阅读` → 选中**被覆盖**为"阅读"
- [ ] `@studying`（数据库已有"studying"小写）→ 命中现有，不重复创建
- [ ] 留空备注点"智能识别" → Toast "请输入备注后再识别"

### - [ ] 步骤 9.4：（可选）push 工作树分支供 PR

```bash
git push -u origin feature/note-directive
```

> 由于此操作把变更发布到远端，**仅在用户明确要求时执行**。

---

## 自检（在执行计划前由人工 / agent 审视）

**1. 规格覆盖：**
- §3 解析规则 → 任务 1 + 2
- §4 UseCase → 任务 3
- §5.1 ViewModel → 任务 5
- §5.2 State 扩展 → 任务 4
- §5.3 onTopButton → 任务 7
- §5.4 回调签名 + 接线 → 任务 6 + 8
- §5.5 Toast 文案 → 任务 7 (buildFeedbackMessage)
- §6 与自动扫描关系 → 任务 7（onTopButton 仍调 applyNoteScan）+ §6 文档化
- §7 文件清单 → 全部出现
- §8 测试策略 → 任务 1-3 覆盖 ParserTest + UseCaseTest，无 UI 测试与现状一致

**2. 占位符扫描：** 已确认无 "TODO / 待定" 字样；每个步骤代码块均含具体可粘贴内容。

**3. 类型一致性：**
- `NoteDirectiveParser.Directive` 字段在任务 1/2/3 始终一致（symbol/name/range）
- `Outcome.Empty` 在任务 3 定义、任务 5 引用，名字一致
- `NoteProcessOutcome.Empty` 在任务 5 定义、任务 6 引用，名字一致
- `applyDirectiveOutcome` 在任务 4 定义、任务 7 调用，签名一致
- `OnProcessNote` typealias 在任务 6 定义、被 6/7/8 引用

---

## 执行交接

计划已完成并保存到 `docs/superpowers/plans/2026-05-14-note-directive-implementation.md`。两种执行方式：

**1. 子代理驱动（推荐）** —— 每个任务调度一个新的子代理，任务间进行审查，快速迭代。
**2. 内联执行** —— 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点。

选哪种方式？

