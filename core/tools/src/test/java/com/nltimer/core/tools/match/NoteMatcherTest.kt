package com.nltimer.core.tools.match

import com.nltimer.core.data.model.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [NoteMatcher] 单元测试 —— 反向扫描语义：`note.contains(candidate)`
 *
 * 覆盖用户给定的全部 17 条核心规则用例 + keywords / 归档 / 大小写 / 单字 name 门槛等扩展场景。
 *
 * 字典：
 * - 活动库：主动学习、放假、会议、会面（全部仅有 name，无 keywords）
 * - 标签库：紧急、重要、沉重（全部仅有 name，无 keywords）
 */
class NoteMatcherTest {

    private val matcher = NoteMatcher()

    private val activities = listOf(
        Activity(id = 1, name = "主动学习"),
        Activity(id = 2, name = "放假"),
        Activity(id = 3, name = "会议"),
        Activity(id = 4, name = "会面"),
    )

    private val tags = listOf(
        tagFixture(id = 10, name = "紧急"),
        tagFixture(id = 11, name = "重要"),
        tagFixture(id = 12, name = "沉重"),
    )

    // ─── 用户规则用例 1-9：纯活动 ───

    @Test
    fun `1_ note 主 should not match any activity`() {
        val result = matcher.scan("主", activities, tags)
        assertNull(result.activityId)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `2_ note 主动 should not match any activity`() {
        val result = matcher.scan("主动", activities, tags)
        assertNull(result.activityId)
    }

    @Test
    fun `3_ note 主动学习 matches activity 主动学习`() {
        val result = matcher.scan("主动学习", activities, tags)
        assertEquals(1L, result.activityId)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `4_ note 会议 matches activity 会议`() {
        val result = matcher.scan("会议", activities, tags)
        assertEquals(3L, result.activityId)
    }

    @Test
    fun `5_ note 会议会面 returns activity 会议 by earliest position`() {
        val result = matcher.scan("会议会面", activities, tags)
        assertEquals(3L, result.activityId)
    }

    @Test
    fun `6_ note 会面会议 returns activity 会面 by earliest position`() {
        val result = matcher.scan("会面会议", activities, tags)
        assertEquals(4L, result.activityId)
    }

    @Test
    fun `7_ note 主动学习会议 returns 主动学习 at pos 0`() {
        val result = matcher.scan("主动学习会议", activities, tags)
        assertEquals(1L, result.activityId)
    }

    @Test
    fun `8_ note 会议主动学习 returns 会议 at pos 0`() {
        val result = matcher.scan("会议主动学习", activities, tags)
        assertEquals(3L, result.activityId)
    }

    @Test
    fun `9_ note 学习主动 has no activity match (reverse order)`() {
        val result = matcher.scan("学习主动", activities, tags)
        assertNull(result.activityId)
    }

    @Test
    fun `note 放假 matches activity 放假`() {
        val result = matcher.scan("放假", activities, tags)
        assertEquals(2L, result.activityId)
    }

    @Test
    fun `note 放假会议 returns 放假 at pos 0`() {
        val result = matcher.scan("放假会议", activities, tags)
        assertEquals(2L, result.activityId)
    }

    // ─── 用户规则用例 10-12：纯标签 ───

    @Test
    fun `10_ note 紧 matches no tag`() {
        val result = matcher.scan("紧", activities, tags)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `11_ note 紧急重要沉重 matches all three tags`() {
        val result = matcher.scan("紧急重要沉重", activities, tags)
        assertEquals(setOf(10L, 11L, 12L), result.tagIds)
        assertNull(result.activityId)
    }

    @Test
    fun `note 紧急重要 matches two tags`() {
        val result = matcher.scan("紧急重要", activities, tags)
        assertEquals(setOf(10L, 11L), result.tagIds)
    }

    @Test
    fun `note 重要紧急 matches both regardless of order`() {
        val result = matcher.scan("重要紧急", activities, tags)
        assertEquals(setOf(10L, 11L), result.tagIds)
    }

    @Test
    fun `12_ note 急紧 has no tag match (reverse order)`() {
        val result = matcher.scan("急紧", activities, tags)
        assertTrue(result.tagIds.isEmpty())
    }

    // ─── 用户规则用例 13-17：混合 ───

    @Test
    fun `13_ note 会议紧急 matches activity 会议 and tag 紧急`() {
        val result = matcher.scan("会议紧急", activities, tags)
        assertEquals(3L, result.activityId)
        assertEquals(setOf(10L), result.tagIds)
    }

    @Test
    fun `14_ note 会议紧急重要 matches activity 会议 and two tags`() {
        val result = matcher.scan("会议紧急重要", activities, tags)
        assertEquals(3L, result.activityId)
        assertEquals(setOf(10L, 11L), result.tagIds)
    }

    @Test
    fun `15_ note 会议会面紧急重要 returns first activity 会议 and two tags`() {
        val result = matcher.scan("会议会面紧急重要", activities, tags)
        assertEquals(3L, result.activityId)
        assertEquals(setOf(10L, 11L), result.tagIds)
    }

    @Test
    fun `note 紧急会议 matches both (tag-first order)`() {
        val result = matcher.scan("紧急会议", activities, tags)
        assertEquals(3L, result.activityId)
        assertEquals(setOf(10L), result.tagIds)
    }

    @Test
    fun `16_ note 会议紧 matches activity but no tag (紧 incomplete)`() {
        val result = matcher.scan("会议紧", activities, tags)
        assertEquals(3L, result.activityId)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `17_ note 会紧急 matches tag 紧急 but no activity (会 incomplete)`() {
        val result = matcher.scan("会紧急", activities, tags)
        assertNull(result.activityId)
        assertEquals(setOf(10L), result.tagIds)
    }

    @Test
    fun `note 主动学习重要沉重 matches activity and two tags`() {
        val result = matcher.scan("主动学习重要沉重", activities, tags)
        assertEquals(1L, result.activityId)
        assertEquals(setOf(11L, 12L), result.tagIds)
    }

    // ─── 边界场景 ───

    @Test
    fun `empty note returns null and empty set`() {
        val result = matcher.scan("", activities, tags)
        assertNull(result.activityId)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `blank-only note returns null and empty set`() {
        val result = matcher.scan("   \n\t  ", activities, tags)
        assertNull(result.activityId)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `empty activities and tags returns null and empty set`() {
        val result = matcher.scan("任意备注内容", emptyList(), emptyList())
        assertNull(result.activityId)
        assertTrue(result.tagIds.isEmpty())
    }

    // ─── name 长度门槛：≥2 才参与 ───

    @Test
    fun `single-char name without keywords is excluded from matching`() {
        val singleCharActivities = listOf(
            Activity(id = 100, name = "读"),
            Activity(id = 101, name = "读书"),
        )
        val result = matcher.scan("今天读书", singleCharActivities, emptyList())
        assertEquals(101L, result.activityId)
    }

    @Test
    fun `single-char name tag is excluded`() {
        val singleCharTags = listOf(
            tagFixture(id = 200, name = "急"),
            tagFixture(id = 201, name = "重要"),
        )
        val result = matcher.scan("急事重要", emptyList(), singleCharTags)
        assertEquals(setOf(201L), result.tagIds)
    }

    // ─── keywords 优先且不受长度门槛限制 ───

    @Test
    fun `keywords non-empty bypasses name match entirely`() {
        val keywordOnlyTags = listOf(
            tagFixture(id = 300, name = "学习", keywords = "读书,编程"),
        )
        val result = matcher.scan("今天学习了一会儿", emptyList(), keywordOnlyTags)
        assertTrue(result.tagIds.isEmpty())
    }

    @Test
    fun `keywords token hit returns tag even when name absent`() {
        val keywordOnlyTags = listOf(
            tagFixture(id = 300, name = "学习", keywords = "读书,编程"),
        )
        val result = matcher.scan("今天读书一小时", emptyList(), keywordOnlyTags)
        assertEquals(setOf(300L), result.tagIds)
    }

    @Test
    fun `keywords single-char token participates without length gate`() {
        val singleKeywordTag = listOf(
            tagFixture(id = 400, name = "阅读", keywords = "读"),
        )
        val result = matcher.scan("今天读了一会儿", emptyList(), singleKeywordTag)
        assertEquals(setOf(400L), result.tagIds)
    }

    @Test
    fun `keywords any-one token hit suffices`() {
        val multiKeywordActivity = listOf(
            Activity(id = 500, name = "运动", keywords = "跑步,健身,游泳"),
        )
        val result = matcher.scan("下班去健身", multiKeywordActivity, emptyList())
        assertEquals(500L, result.activityId)
    }

    @Test
    fun `keywords activity position is computed from earliest keyword hit`() {
        val mixed = listOf(
            Activity(id = 600, name = "活动甲", keywords = "甲"),
            Activity(id = 601, name = "活动乙", keywords = "乙"),
        )
        val result = matcher.scan("乙的甲", mixed, emptyList())
        assertEquals(601L, result.activityId)
    }

    // ─── 已归档项不扫描 ───

    @Test
    fun `archived activity is excluded`() {
        val archivedAndActive = listOf(
            Activity(id = 700, name = "读书", isArchived = true),
            Activity(id = 701, name = "编程"),
        )
        val result = matcher.scan("今天读书也编程", archivedAndActive, emptyList())
        assertEquals(701L, result.activityId)
    }

    @Test
    fun `archived tag is excluded`() {
        val mixedTags = listOf(
            tagFixture(id = 800, name = "紧急", isArchived = true),
            tagFixture(id = 801, name = "重要"),
        )
        val result = matcher.scan("紧急重要", emptyList(), mixedTags)
        assertEquals(setOf(801L), result.tagIds)
    }

    // ─── 大小写不敏感 ───

    @Test
    fun `case insensitive match for english activity name`() {
        val englishActivities = listOf(
            Activity(id = 900, name = "Workout"),
        )
        val result = matcher.scan("今天 WORKOUT 一小时", englishActivities, emptyList())
        assertEquals(900L, result.activityId)
    }

    @Test
    fun `case insensitive match for english keyword token`() {
        val englishTags = listOf(
            tagFixture(id = 901, name = "Health", keywords = "fitness,GYM"),
        )
        val result = matcher.scan("Going to gym today", emptyList(), englishTags)
        assertEquals(setOf(901L), result.tagIds)
    }
}
