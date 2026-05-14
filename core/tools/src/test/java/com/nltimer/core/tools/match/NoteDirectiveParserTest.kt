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
