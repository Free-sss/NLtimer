package com.nltimer.core.tools.match

import com.nltimer.core.data.model.Activity
import com.nltimer.core.data.model.ActivityGroup
import com.nltimer.core.data.model.Tag
import com.nltimer.core.data.repository.ActivityRepository
import com.nltimer.core.data.repository.TagRepository
import com.nltimer.core.tools.ToolError
import com.nltimer.core.tools.ToolResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [MatchActivitiesAndTagsTool] 单元测试
 *
 * 重点覆盖 UX 关键语义：
 * - keywords 优先；keywords 非空时不回退 name
 * - keywords 为空时回退 name
 * - 正则解析失败 → ValidationError
 * - 默认排除已归档项
 */
class MatchActivitiesAndTagsToolTest {

    @Test
    fun `keywords match takes precedence and reports matchedField=keywords`() = runTest {
        val tool = buildTool(
            activities = listOf(
                activity(id = 1, name = "X", keywords = "跑步,锻炼"),
            ),
            tags = emptyList(),
        )

        val result = tool.execute(mapOf("query" to "跑"))

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data as Map<*, *>
        val activities = data["activities"] as List<*>
        assertEquals(1, activities.size)
        val first = activities.first() as Map<*, *>
        assertEquals(1L, first["id"])
        assertEquals("keywords", first["matchedField"])
    }

    @Test
    fun `keywords non-empty without hit does NOT fall back to name`() = runTest {
        // keywords="阅读" 不含"跑"，name="跑步" 含"跑" —— 不应命中（明确的 UX 语义）
        val tool = buildTool(
            activities = listOf(
                activity(id = 1, name = "跑步", keywords = "阅读"),
            ),
            tags = emptyList(),
        )

        val result = tool.execute(mapOf("query" to "跑"))

        assertTrue(result is ToolResult.Success)
        val activities = ((result as ToolResult.Success).data as Map<*, *>)["activities"] as List<*>
        assertTrue("keywords 非空时不回退 name", activities.isEmpty())
    }

    @Test
    fun `keywords blank falls back to matching name with matchedField=name`() = runTest {
        val tool = buildTool(
            activities = listOf(
                activity(id = 1, name = "跑步", keywords = null),
                activity(id = 2, name = "阅读", keywords = ""),
            ),
            tags = emptyList(),
        )

        val result = tool.execute(mapOf("query" to "跑"))

        assertTrue(result is ToolResult.Success)
        val activities = ((result as ToolResult.Success).data as Map<*, *>)["activities"] as List<*>
        assertEquals(1, activities.size)
        val hit = activities.first() as Map<*, *>
        assertEquals(1L, hit["id"])
        assertEquals("name", hit["matchedField"])
    }

    @Test
    fun `tags participate when scope is both`() = runTest {
        val tool = buildTool(
            activities = emptyList(),
            tags = listOf(
                tag(id = 1, name = "运动", keywords = "跑步,健身"),
            ),
        )

        val result = tool.execute(mapOf("query" to "健身"))

        assertTrue(result is ToolResult.Success)
        val tags = ((result as ToolResult.Success).data as Map<*, *>)["tags"] as List<*>
        assertEquals(1, tags.size)
        assertEquals("keywords", (tags.first() as Map<*, *>)["matchedField"])
    }

    @Test
    fun `scope=activities skips tags`() = runTest {
        val tool = buildTool(
            activities = listOf(activity(id = 1, name = "跑步", keywords = null)),
            tags = listOf(tag(id = 9, name = "跑步标签", keywords = null)),
        )

        val result = tool.execute(mapOf("query" to "跑", "scope" to "activities"))

        assertTrue(result is ToolResult.Success)
        val data = (result as ToolResult.Success).data as Map<*, *>
        val activities = data["activities"] as List<*>
        val tags = data["tags"] as List<*>
        assertEquals(1, activities.size)
        assertTrue(tags.isEmpty())
    }

    @Test
    fun `useRegex=true with valid pattern matches`() = runTest {
        val tool = buildTool(
            activities = listOf(
                activity(id = 1, name = "跑步早起", keywords = null),
                activity(id = 2, name = "深夜阅读", keywords = null),
            ),
            tags = emptyList(),
        )

        val result = tool.execute(mapOf("query" to "^跑", "useRegex" to true))

        assertTrue(result is ToolResult.Success)
        val activities = ((result as ToolResult.Success).data as Map<*, *>)["activities"] as List<*>
        assertEquals(1, activities.size)
        assertEquals(1L, (activities.first() as Map<*, *>)["id"])
    }

    @Test
    fun `useRegex=true with invalid pattern returns ValidationError`() = runTest {
        val tool = buildTool(activities = emptyList(), tags = emptyList())

        val result = tool.execute(mapOf("query" to "[unclosed", "useRegex" to true))

        assertTrue(result is ToolResult.Error)
        val error = (result as ToolResult.Error).error
        assertTrue(error is ToolError.ValidationError)
        assertTrue((error as ToolError.ValidationError).message.contains("正则"))
    }

    @Test
    fun `archived items excluded by default`() = runTest {
        val tool = buildTool(
            activitiesActive = listOf(activity(id = 1, name = "跑步", keywords = null)),
            activitiesAll = listOf(
                activity(id = 1, name = "跑步", keywords = null),
                activity(id = 2, name = "跑步存档", keywords = null, isArchived = true),
            ),
            tags = emptyList(),
        )

        val result = tool.execute(mapOf("query" to "跑"))

        assertTrue(result is ToolResult.Success)
        val activities = ((result as ToolResult.Success).data as Map<*, *>)["activities"] as List<*>
        // 默认排除归档：只有 id=1 命中
        assertEquals(1, activities.size)
        assertEquals(1L, (activities.first() as Map<*, *>)["id"])
    }

    @Test
    fun `archived included when includeArchived=true`() = runTest {
        val tool = buildTool(
            activitiesActive = listOf(activity(id = 1, name = "跑步", keywords = null)),
            activitiesAll = listOf(
                activity(id = 1, name = "跑步", keywords = null),
                activity(id = 2, name = "跑步存档", keywords = null, isArchived = true),
            ),
            tags = emptyList(),
        )

        val result = tool.execute(mapOf("query" to "跑", "includeArchived" to true))

        assertTrue(result is ToolResult.Success)
        val activities = ((result as ToolResult.Success).data as Map<*, *>)["activities"] as List<*>
        assertEquals(2, activities.size)
    }

    // --- 测试夹具与 fakes ---

    private fun activity(
        id: Long,
        name: String,
        keywords: String?,
        isArchived: Boolean = false,
    ) = Activity(
        id = id,
        name = name,
        iconKey = null,
        keywords = keywords,
        groupId = null,
        isPreset = false,
        isArchived = isArchived,
        archivedAt = null,
        color = null,
        usageCount = 0,
    )

    private fun tag(
        id: Long,
        name: String,
        keywords: String?,
        isArchived: Boolean = false,
    ) = Tag(
        id = id,
        name = name,
        color = null,
        iconKey = null,
        category = null,
        priority = 0,
        usageCount = 0,
        sortOrder = 0,
        isArchived = isArchived,
        archivedAt = null,
        keywords = keywords,
    )

    private fun buildTool(
        activities: List<Activity>,
        tags: List<Tag>,
    ): MatchActivitiesAndTagsTool = buildTool(
        activitiesActive = activities,
        activitiesAll = activities,
        tags = tags,
    )

    private fun buildTool(
        activitiesActive: List<Activity>,
        activitiesAll: List<Activity>,
        tags: List<Tag>,
    ): MatchActivitiesAndTagsTool {
        val activityRepo = FakeActivityRepository(active = activitiesActive, all = activitiesAll)
        val tagRepo = FakeTagRepository(active = tags, all = tags)
        return MatchActivitiesAndTagsTool(activityRepo, tagRepo)
    }

    private class FakeActivityRepository(
        private val active: List<Activity>,
        private val all: List<Activity>,
    ) : ActivityRepository {
        override fun getAllActive(): Flow<List<Activity>> = flowOf(active)
        override fun getAll(): Flow<List<Activity>> = flowOf(all)
        override fun getAllGroups(): Flow<List<ActivityGroup>> = flowOf(emptyList())
        override fun search(query: String): Flow<List<Activity>> = flowOf(emptyList())
        override suspend fun getById(id: Long): Activity? = all.firstOrNull { it.id == id }
        override suspend fun getByName(name: String): Activity? = all.firstOrNull { it.name == name }
        override suspend fun insert(activity: Activity): Long = activity.id
        override suspend fun update(activity: Activity) = Unit
        override suspend fun setArchived(id: Long, archived: Boolean) = Unit
    }

    private class FakeTagRepository(
        private val active: List<Tag>,
        private val all: List<Tag>,
    ) : TagRepository {
        override fun getAllActive(): Flow<List<Tag>> = flowOf(active)
        override fun getAll(): Flow<List<Tag>> = flowOf(all)
        override fun getByCategory(category: String): Flow<List<Tag>> = flowOf(emptyList())
        override fun search(query: String): Flow<List<Tag>> = flowOf(emptyList())
        override fun getByActivityId(activityId: Long): Flow<List<Tag>> = flowOf(emptyList())
        override suspend fun getById(id: Long): Tag? = all.firstOrNull { it.id == id }
        override suspend fun getByName(name: String): Tag? = all.firstOrNull { it.name == name }
        override suspend fun insert(tag: Tag): Long = tag.id
        override suspend fun update(tag: Tag) = Unit
        override suspend fun setArchived(id: Long, archived: Boolean) = Unit
        override fun getDistinctCategories(): Flow<List<String>> = flowOf(emptyList())
        override suspend fun renameCategory(oldName: String, newName: String) = Unit
        override suspend fun resetCategory(category: String) = Unit
    }
}
