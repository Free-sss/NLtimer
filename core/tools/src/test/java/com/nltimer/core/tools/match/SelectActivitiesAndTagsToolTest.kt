package com.nltimer.core.tools.match

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.tools.ToolError
import com.nltimer.core.tools.ToolResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [SelectActivitiesAndTagsTool] 单元测试 —— token 精确相等语义
 *
 * 关键覆盖（直接复刻用户原话场景）：
 * - 输入 "天" **不会** 命中 今天/明天/后天 任何一个（Search vs Select 最关键的差异）
 * - 输入 "计划" 同时命中名为"计划"的活动 + keywords 含"计划"的标签「计划目标」
 * - keywords 优先 + 非空时不回退 name（输入"计划目标"不命中 keywords="计划" 的标签）
 * - 多 keywords token 任一相等命中（"目标" 命中 keywords="计划,目标"）
 * - 正则 useRegex=true 用 matches 完整覆盖整段；子串不命中
 * - scope/includeArchived/caseSensitive/非法正则
 */
class SelectActivitiesAndTagsToolTest {

    private lateinit var activityRepository: ActivityRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var tool: SelectActivitiesAndTagsTool

    @Before
    fun setup() {
        activityRepository = mockk()
        tagRepository = mockk()
        tool = SelectActivitiesAndTagsTool(activityRepository, tagRepository)
    }

    /** 用户原话核心场景：输入"天" 在选择模式下不应命中 今天/明天/后天 */
    @Test
    fun `query 天 does NOT match any of today tomorrow dayAfter in select mode`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "今天"),
                Activity(id = 2, name = "明天"),
                Activity(id = 3, name = "后天"),
                Activity(id = 4, name = "计划"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "天"))

        val data = result.successData()
        val activities = data.activityRows()
        assertTrue("substring must NOT hit any name in select mode", activities.isEmpty())
    }

    /** 用户原话核心需求：输入"计划" 同时命中活动「计划」+ keywords 含计划的标签「计划目标」 */
    @Test
    fun `query 计划 matches activity 计划 AND tag 计划目标 with keyword 计划`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "今天"),
                Activity(id = 2, name = "明天"),
                Activity(id = 3, name = "后天"),
                Activity(id = 4, name = "计划"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(
            listOf(
                tagFixture(id = 1, name = "计划目标", keywords = "计划"),
                tagFixture(id = 2, name = "工作"),
            )
        )

        val result = tool.execute(mapOf("query" to "计划"))

        val data = result.successData()
        val activities = data.activityRows()
        val tags = data.tagRows()

        assertEquals(1, activities.size)
        assertEquals(4L, activities[0]["id"])
        assertEquals("name", activities[0]["matchedField"])

        assertEquals(1, tags.size)
        assertEquals(1L, tags[0]["id"])
        assertEquals("keywords", tags[0]["matchedField"])
    }

    /** keywords 非空时不回退到 name：输入「计划目标」不应命中 keywords="计划" 的标签 */
    @Test
    fun `non-empty keywords does not fall back to name field`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(emptyList())
        every { tagRepository.getAllActive() } returns flowOf(
            listOf(
                tagFixture(id = 1, name = "计划目标", keywords = "计划"),
            )
        )

        val result = tool.execute(mapOf("query" to "计划目标"))

        val data = result.successData()
        val tags = data.tagRows()
        assertTrue(tags.isEmpty())
    }

    /** 多个 keywords token 任一相等即命中 */
    @Test
    fun `any keyword token equality wins`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "活动A", keywords = "跑步,健身,游泳"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "健身"))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(1, activities.size)
        assertEquals("keywords", activities[0]["matchedField"])
    }

    /** keywords 子串不命中（必须 token 完全相等）：输入"健"不命中 keywords="健身" */
    @Test
    fun `keyword substring does not match in select mode`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "活动A", keywords = "健身"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "健"))

        val data = result.successData()
        val activities = data.activityRows()
        assertTrue(activities.isEmpty())
    }

    /** 空 keywords 时回退 name 字段做精确相等匹配 */
    @Test
    fun `empty keywords falls back to exact name equality`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "工作"),
                Activity(id = 2, name = "工作日"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "工作"))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(1, activities.size)
        assertEquals(1L, activities[0]["id"])
    }

    @Test
    fun `query case insensitive by default`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "Workout"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "WORKOUT"))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(1, activities.size)
    }

    @Test
    fun `caseSensitive true rejects different case`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "Workout"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "WORKOUT", "caseSensitive" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertTrue(activities.isEmpty())
    }

    /** 正则模式：matches 必须完整覆盖整段；只匹配到子串不算命中 */
    @Test
    fun `useRegex true uses matches full string equality not substring`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "今天"),
                Activity(id = 2, name = "明天"),
                Activity(id = 3, name = "运动"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to ".天", "useRegex" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(setOf(1L, 2L), activities.map { it["id"] as Long }.toSet())
    }

    /** 正则模式下纯子串不命中：query="计" 不应命中"计划" */
    @Test
    fun `useRegex true substring without anchors does not match longer string`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "计划"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "计", "useRegex" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertTrue(activities.isEmpty())
    }

    @Test
    fun `invalid regex returns ValidationError`() = runTest {
        val result = tool.execute(mapOf("query" to "[invalid", "useRegex" to true))

        assertTrue(result is ToolResult.Error)
        assertTrue((result as ToolResult.Error).error is ToolError.ValidationError)
    }

    @Test
    fun `scope tags only queries tag repository`() = runTest {
        every { tagRepository.getAllActive() } returns flowOf(
            listOf(
                tagFixture(id = 1, name = "工作"),
            )
        )

        val result = tool.execute(mapOf("query" to "工作", "scope" to "tags"))

        val data = result.successData()
        val activities = data.activityRows()
        val tags = data.tagRows()
        assertTrue(activities.isEmpty())
        assertEquals(1, tags.size)
    }

    @Test
    fun `includeArchived true uses getAll instead of getAllActive`() = runTest {
        every { activityRepository.getAll() } returns flowOf(
            listOf(
                Activity(id = 1, name = "归档项", isArchived = true),
            )
        )
        every { tagRepository.getAll() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "归档项", "includeArchived" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(1, activities.size)
    }

    @Test
    fun `result data includes mode field equal to select`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(emptyList())
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "x"))

        val data = result.successData()
        assertEquals("select", data["mode"])
    }
}
