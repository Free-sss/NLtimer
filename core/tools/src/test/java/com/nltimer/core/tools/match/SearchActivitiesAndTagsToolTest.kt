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
 * [SearchActivitiesAndTagsTool] 单元测试 —— substring（子串）语义
 */
class SearchActivitiesAndTagsToolTest {

    private lateinit var activityRepository: ActivityRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var tool: SearchActivitiesAndTagsTool

    @Before
    fun setup() {
        activityRepository = mockk()
        tagRepository = mockk()
        tool = SearchActivitiesAndTagsTool(activityRepository, tagRepository)
    }

    @Test
    fun `query 天 hits all three day activities by substring`() = runTest {
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
        assertEquals("search", data["mode"])
        val activities = data.activityRows()
        assertEquals(setOf(1L, 2L, 3L), activities.map { it["id"] as Long }.toSet())
        activities.forEach { assertEquals("name", it["matchedField"]) }
    }

    @Test
    fun `keywords field has priority over name`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "运动", keywords = "跑步,健身"),
                Activity(id = 2, name = "工作"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "跑"))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(1, activities.size)
        assertEquals(1L, activities[0]["id"])
        assertEquals("keywords", activities[0]["matchedField"])
    }

    @Test
    fun `non-empty keywords skips name fallback`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "计划", keywords = "明天"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "计划"))

        val data = result.successData()
        val activities = data.activityRows()
        assertTrue(activities.isEmpty())
    }

    @Test
    fun `query case insensitive by default`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "Workout"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "WORK"))

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

        val result = tool.execute(mapOf("query" to "WORK", "caseSensitive" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertTrue(activities.isEmpty())
    }

    @Test
    fun `useRegex true uses containsMatchIn for substring scan`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "今天"),
                Activity(id = 2, name = "明天"),
                Activity(id = 3, name = "工作"),
            )
        )
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "天\$", "useRegex" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(setOf(1L, 2L), activities.map { it["id"] as Long }.toSet())
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

        val result = tool.execute(mapOf("query" to "工", "scope" to "tags"))

        val data = result.successData()
        val activities = data.activityRows()
        val tags = data.tagRows()
        assertTrue(activities.isEmpty())
        assertEquals(1, tags.size)
    }

    @Test
    fun `scope activities only queries activity repository`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(
            listOf(
                Activity(id = 1, name = "工作"),
            )
        )

        val result = tool.execute(mapOf("query" to "工", "scope" to "activities"))

        val data = result.successData()
        val activities = data.activityRows()
        val tags = data.tagRows()
        assertEquals(1, activities.size)
        assertTrue(tags.isEmpty())
    }

    @Test
    fun `includeArchived true uses getAll instead of getAllActive`() = runTest {
        every { activityRepository.getAll() } returns flowOf(
            listOf(
                Activity(id = 1, name = "已归档", isArchived = true),
            )
        )
        every { tagRepository.getAll() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "归", "includeArchived" to true))

        val data = result.successData()
        val activities = data.activityRows()
        assertEquals(1, activities.size)
    }

    @Test
    fun `result data includes mode field equal to search`() = runTest {
        every { activityRepository.getAllActive() } returns flowOf(emptyList())
        every { tagRepository.getAllActive() } returns flowOf(emptyList())

        val result = tool.execute(mapOf("query" to "x"))

        val data = result.successData()
        assertEquals("search", data["mode"])
    }
}
