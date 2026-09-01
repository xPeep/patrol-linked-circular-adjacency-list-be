package cz.csas.datastructures.patrol.datastructure

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The iterator is the single most dangerous part of a circular structure: a naive
 * implementation loops forever. These tests make sure it walks the whole list
 * exactly once and then stops.
 */
@DisplayName("CircularLinkedList - iterator")
class CircularLinkedListIteratorTest {

    private fun listOfNames(vararg names: String): CircularLinkedList<String> =
        CircularLinkedList<String>().apply { names.forEach { addLast(it) } }

    @Test
    fun `an empty list iterates over nothing`() {
        val iterator = CircularLinkedList<String>().iterator()

        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun `a single item is yielded exactly once`() {
        val iterator = listOfNames("Entrance").iterator()

        assertTrue(iterator.hasNext())
        assertEquals("Entrance", iterator.next())
        assertFalse(iterator.hasNext())
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    @Timeout(10)
    fun `iteration stops after exactly one cycle`() {
        run {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            val seen = ArrayList<String>()
            for (item in list) {
                seen.add(item)
                check(seen.size <= 100) { "The iterator never stopped, it looped around the circle" }
            }

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), seen)
        }
    }

    @Test
    fun `iteration always starts at first, no matter where the cursor stands`() {
        val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

        list.next()
        list.next()
        assertEquals("Charging", list.current())

        assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), list.toList())
    }

    @Test
    fun `iteration yields the same order after a full cursor cycle`() {
        val list = listOfNames("Entrance", "Shelf A", "Charging")
        val before = list.toList()

        repeat(7) { list.next() }

        assertEquals(before, list.toList())
    }

    @Test
    fun `hasNext does not consume elements when called repeatedly`() {
        val iterator = listOfNames("Entrance", "Shelf A").iterator()

        repeat(5) { assertTrue(iterator.hasNext()) }
        assertEquals("Entrance", iterator.next())
        repeat(5) { assertTrue(iterator.hasNext()) }
        assertEquals("Shelf A", iterator.next())
        repeat(5) { assertFalse(iterator.hasNext()) }
    }

    @Test
    fun `next may be called without hasNext and still stops at the end`() {
        val iterator = listOfNames("A", "B").iterator()

        assertEquals("A", iterator.next())
        assertEquals("B", iterator.next())
        assertFailsWith<NoSuchElementException> { iterator.next() }
    }

    @Test
    fun `two iterators are independent of each other`() {
        val list = listOfNames("A", "B", "C")

        val left = list.iterator()
        val right = list.iterator()

        assertEquals("A", left.next())
        assertEquals("B", left.next())
        assertEquals("A", right.next())
        assertEquals("C", left.next())
        assertEquals("B", right.next())
        assertEquals("C", right.next())
        assertFalse(left.hasNext())
        assertFalse(right.hasNext())
    }

    @Test
    fun `the list can be iterated over and over again`() {
        val list = listOfNames("A", "B", "C")

        repeat(3) {
            assertEquals(listOf("A", "B", "C"), list.toList())
        }
    }

    @Test
    fun `standard library helpers built on the iterator work`() {
        val list = listOfNames("Entrance", "Shelf A", "Charging")

        assertEquals(3, list.count())
        assertTrue(list.contains("Shelf A"))
        assertFalse(list.contains("Nowhere"))
        assertEquals("Entrance, Shelf A, Charging", list.joinToString())
        assertEquals(listOf("SHELF A"), list.filter { it.startsWith("Shelf") }.map { it.uppercase() })
    }

    @Test
    @Timeout(30)
    fun `iteration of a large list terminates`() {
        run {
            val list = CircularLinkedList<Int>()
            repeat(10_000) { list.addLast(it) }

            var counted = 0
            for (item in list) {
                counted++
                check(counted <= 10_000) { "The iterator did not stop after one cycle" }
            }

            assertEquals(10_000, counted)
        }
    }

    @Test
    fun `iteration reflects the current content of the list`() {
        val list = listOfNames("A", "B", "C")

        list.next()
        list.removeCurrent()
        list.addAfterCurrent("X")

        assertEquals(listOf("A", "C", "X"), list.toList())
    }

    @Test
    fun `adding during iteration is detected`() {
        val list = listOfNames("A", "B", "C")
        val iterator = list.iterator()
        iterator.next()

        list.addLast("D")

        assertFailsWith<ConcurrentModificationException> { iterator.next() }
    }

    @Test
    fun `removing during iteration is detected`() {
        val list = listOfNames("A", "B", "C")
        val iterator = list.iterator()
        iterator.next()

        list.removeCurrent()

        assertFailsWith<ConcurrentModificationException> { iterator.next() }
    }

    @Test
    fun `moving the cursor during iteration is not a structural change`() {
        val list = listOfNames("A", "B", "C")
        val iterator = list.iterator()

        assertEquals("A", iterator.next())
        list.next()
        list.previous()
        assertEquals("B", iterator.next())
        assertEquals("C", iterator.next())
        assertFalse(iterator.hasNext())
    }
}
