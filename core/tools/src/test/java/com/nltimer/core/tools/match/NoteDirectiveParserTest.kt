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
}
