package cz.csas.datastructures.patrol.datastructure

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The behavioural contract of the circular doubly linked list.
 *
 * Everything here is observable through the public API only. The internal shape of
 * the structure is checked separately in [CircularLinkedListStructureTest].
 */
@DisplayName("CircularLinkedList - behaviour")
class CircularLinkedListBehaviourTest {

    private data class Checkpoint(val id: Int, val name: String)

    private fun listOfNames(vararg names: String): CircularLinkedList<String> =
        CircularLinkedList<String>().apply { names.forEach { addLast(it) } }

    @Nested
    @DisplayName("an empty list")
    inner class EmptyList {

        private val list = CircularLinkedList<String>()

        @Test
        fun `reports being empty`() {
            assertTrue(list.isEmpty())
        }

        @Test
        fun `has size zero`() {
            assertEquals(0, list.size)
        }

        @Test
        fun `yields no elements`() {
            assertEquals(emptyList(), list.toList())
        }

        @Test
        fun `current fails with NoSuchElementException`() {
            assertFailsWith<NoSuchElementException> { list.current() }
        }

        @Test
        fun `next fails with NoSuchElementException`() {
            assertFailsWith<NoSuchElementException> { list.next() }
        }

        @Test
        fun `previous fails with NoSuchElementException`() {
            assertFailsWith<NoSuchElementException> { list.previous() }
        }

        @Test
        fun `removeCurrent fails with NoSuchElementException`() {
            assertFailsWith<NoSuchElementException> { list.removeCurrent() }
        }

        @Test
        fun `stays empty after every failed operation`() {
            runCatching { list.next() }
            runCatching { list.previous() }
            runCatching { list.removeCurrent() }
            runCatching { list.current() }
            assertTrue(list.isEmpty())
            assertEquals(0, list.size)
        }
    }

    @Nested
    @DisplayName("adding the very first item")
    inner class FirstItem {

        @Test
        fun `addLast on an empty list makes the item current`() {
            val list = CircularLinkedList<String>()

            list.addLast("Entrance")

            assertFalse(list.isEmpty())
            assertEquals(1, list.size)
            assertEquals("Entrance", list.current())
            assertEquals(listOf("Entrance"), list.toList())
        }

        @Test
        fun `addAfterCurrent on an empty list makes the item current`() {
            val list = CircularLinkedList<String>()

            list.addAfterCurrent("Entrance")

            assertFalse(list.isEmpty())
            assertEquals(1, list.size)
            assertEquals("Entrance", list.current())
            assertEquals(listOf("Entrance"), list.toList())
        }

        @Test
        fun `the only item is its own next and previous`() {
            val list = listOfNames("Entrance")

            assertEquals("Entrance", list.next())
            assertEquals("Entrance", list.previous())
            assertEquals("Entrance", list.current())
        }

        @Test
        fun `moving around a single item never changes the size`() {
            val list = listOfNames("Entrance")

            repeat(10) { list.next() }
            repeat(10) { list.previous() }

            assertEquals(1, list.size)
            assertEquals("Entrance", list.current())
        }
    }

    @Nested
    @DisplayName("addLast")
    inner class AddLast {

        @Test
        fun `appends at the end and keeps the insertion order`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), list.toList())
            assertEquals(4, list.size)
        }

        @Test
        fun `does not move the current item`() {
            val list = listOfNames("Entrance")

            list.addLast("Shelf A")
            list.addLast("Charging")

            assertEquals("Entrance", list.current())
        }

        @Test
        fun `keeps the current item even when the cursor is in the middle`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging")
            list.next()

            list.addLast("Dispatch")

            assertEquals("Shelf A", list.current())
            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), list.toList())
        }

        @Test
        fun `the appended item becomes the new last one`() {
            val list = listOfNames("Entrance", "Shelf A")

            list.addLast("Dispatch")

            // From first, three steps back reaches first again through the new tail.
            assertEquals("Entrance", list.current())
            assertEquals("Dispatch", list.previous())
        }

        @Test
        fun `accepts duplicated values`() {
            val list = listOfNames("Shelf A", "Shelf A", "Shelf A")

            assertEquals(3, list.size)
            assertEquals(listOf("Shelf A", "Shelf A", "Shelf A"), list.toList())
        }
    }

    @Nested
    @DisplayName("addAfterCurrent")
    inner class AddAfterCurrent {

        @Test
        fun `inserts right behind the cursor`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")

            list.addAfterCurrent("Charging")

            assertEquals(listOf("Entrance", "Charging", "Shelf A", "Dispatch"), list.toList())
        }

        @Test
        fun `never moves the cursor`() {
            val list = listOfNames("Entrance", "Shelf A")

            list.addAfterCurrent("Charging")

            assertEquals("Entrance", list.current())
        }

        @Test
        fun `inserting behind the last item makes the new item the last one`() {
            val list = listOfNames("Entrance", "Shelf A")
            list.next()
            assertEquals("Shelf A", list.current())

            list.addAfterCurrent("Dispatch")

            assertEquals(listOf("Entrance", "Shelf A", "Dispatch"), list.toList())
            assertEquals("Shelf A", list.current())
            assertEquals("Dispatch", list.next())
            assertEquals("Entrance", list.next())
        }

        @Test
        fun `repeated inserts behind the same cursor end up in reverse order`() {
            val list = listOfNames("Entrance")

            list.addAfterCurrent("A")
            list.addAfterCurrent("B")
            list.addAfterCurrent("C")

            assertEquals(listOf("Entrance", "C", "B", "A"), list.toList())
        }

        @Test
        fun `inserting in the middle keeps the circle closed`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")
            list.next()

            list.addAfterCurrent("Charging")

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), list.toList())
            assertEquals("Charging", list.next())
            assertEquals("Dispatch", list.next())
            assertEquals("Entrance", list.next())
        }
    }

    @Nested
    @DisplayName("next")
    inner class Next {

        @Test
        fun `returns the new current item`() {
            val list = listOfNames("Entrance", "Shelf A")

            val moved = list.next()

            assertEquals("Shelf A", moved)
            assertEquals("Shelf A", list.current())
        }

        @Test
        fun `wraps around from the last item to the first one`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")

            list.next()
            list.next()
            assertEquals("Dispatch", list.current())

            assertEquals("Entrance", list.next())
        }

        @Test
        fun `a full cycle returns to the starting point`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")
            val start = list.current()

            repeat(list.size) { list.next() }

            assertEquals(start, list.current())
        }

        @Test
        fun `many full cycles return to the starting point`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging")

            repeat(list.size * 37) { list.next() }

            assertEquals("Entrance", list.current())
        }

        @Test
        fun `visits every item exactly once per cycle`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            val visited = (1..list.size).map { list.next() }

            assertEquals(listOf("Shelf A", "Charging", "Dispatch", "Entrance"), visited)
        }

        @Test
        fun `does not change the size`() {
            val list = listOfNames("Entrance", "Shelf A")

            repeat(5) { list.next() }

            assertEquals(2, list.size)
        }
    }

    @Nested
    @DisplayName("previous")
    inner class Previous {

        @Test
        fun `returns the new current item`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")
            list.next()

            val moved = list.previous()

            assertEquals("Entrance", moved)
            assertEquals("Entrance", list.current())
        }

        @Test
        fun `wraps around from the first item to the last one`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")

            assertEquals("Dispatch", list.previous())
        }

        @Test
        fun `a full backward cycle returns to the starting point`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            repeat(list.size) { list.previous() }

            assertEquals("Entrance", list.current())
        }

        @Test
        fun `is the exact inverse of next`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            repeat(3) { list.next() }
            repeat(3) { list.previous() }

            assertEquals("Entrance", list.current())
        }

        @Test
        fun `visits every item exactly once per backward cycle`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            val visited = (1..list.size).map { list.previous() }

            assertEquals(listOf("Dispatch", "Charging", "Shelf A", "Entrance"), visited)
        }
    }

    @Nested
    @DisplayName("removeCurrent")
    inner class RemoveCurrent {

        @Test
        fun `returns the removed item`() {
            val list = listOfNames("Entrance", "Shelf A")

            assertEquals("Entrance", list.removeCurrent())
        }

        @Test
        fun `moves the cursor onto the successor`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")
            list.next()

            list.removeCurrent()

            assertEquals("Dispatch", list.current())
            assertEquals(listOf("Entrance", "Dispatch"), list.toList())
        }

        @Test
        fun `removing the first item promotes the second one`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")

            list.removeCurrent()

            assertEquals("Shelf A", list.current())
            assertEquals(listOf("Shelf A", "Dispatch"), list.toList())
            assertEquals("Dispatch", list.previous())
        }

        @Test
        fun `removing the last item wraps the cursor to the first one`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")
            list.previous()
            assertEquals("Dispatch", list.current())

            list.removeCurrent()

            assertEquals("Entrance", list.current())
            assertEquals(listOf("Entrance", "Shelf A"), list.toList())
            assertEquals("Shelf A", list.previous())
        }

        @Test
        fun `removing the only item empties the list`() {
            val list = listOfNames("Entrance")

            assertEquals("Entrance", list.removeCurrent())

            assertTrue(list.isEmpty())
            assertEquals(0, list.size)
            assertEquals(emptyList(), list.toList())
            assertFailsWith<NoSuchElementException> { list.current() }
        }

        @Test
        fun `shrinks the list by exactly one item`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging")

            list.removeCurrent()

            assertEquals(2, list.size)
        }

        @Test
        fun `removing everything one by one ends with an empty list`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            val removed = (1..4).map { list.removeCurrent() }

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), removed)
            assertTrue(list.isEmpty())
            assertFailsWith<NoSuchElementException> { list.removeCurrent() }
        }

        @Test
        fun `the list is fully usable again after being emptied`() {
            val list = listOfNames("Entrance", "Shelf A")
            list.removeCurrent()
            list.removeCurrent()
            assertTrue(list.isEmpty())

            list.addLast("Fresh start")

            assertEquals(1, list.size)
            assertEquals("Fresh start", list.current())
            assertEquals("Fresh start", list.next())
            assertEquals(listOf("Fresh start"), list.toList())
        }

        @Test
        fun `removes exactly one occurrence of a duplicated value`() {
            val list = listOfNames("Shelf A", "Shelf A", "Shelf A")

            list.removeCurrent()

            assertEquals(2, list.size)
            assertEquals(listOf("Shelf A", "Shelf A"), list.toList())
        }

        @Test
        fun `removes the item the cursor stands on, not the first match`() {
            val list = listOfNames("Duplicate", "Unique", "Duplicate")
            list.next()
            list.next()

            list.removeCurrent()

            assertEquals(listOf("Duplicate", "Unique"), list.toList())
            assertEquals("Duplicate", list.current())
        }
    }

    @Nested
    @DisplayName("mixed realistic scenarios")
    inner class MixedScenarios {

        @Test
        fun `an operator builds a route and walks it`() {
            val list = CircularLinkedList<String>()

            list.addAfterCurrent("Entrance")
            list.next()
            list.addAfterCurrent("Shelf A")
            list.next()
            list.addAfterCurrent("Charging")
            list.next()
            list.addAfterCurrent("Dispatch")

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), list.toList())
            assertEquals("Charging", list.current())
        }

        @Test
        fun `removing while walking keeps the route consistent`() {
            val list = listOfNames("A", "B", "C", "D", "E")

            list.next()
            list.next()
            list.removeCurrent()
            list.previous()
            list.removeCurrent()

            assertEquals(listOf("A", "D", "E"), list.toList())
            assertEquals("D", list.current())
        }

        @Test
        fun `insertion after the cursor followed by a full cycle sees the new item`() {
            val list = listOfNames("A", "B", "C")
            list.next()

            list.addAfterCurrent("NEW")

            val seen = (1..list.size).map { list.next() }
            assertEquals(listOf("NEW", "C", "A", "B"), seen)
        }

        @Test
        fun `the list survives a long sequence of operations`() {
            val list = CircularLinkedList<Int>()

            repeat(100) { list.addLast(it) }
            repeat(50) { list.next() }
            repeat(25) { list.removeCurrent() }
            repeat(10) { list.addAfterCurrent(-1) }

            assertEquals(85, list.size)
            assertEquals(85, list.toList().size)
            assertEquals(10, list.toList().count { it == -1 })
        }
    }

    @Nested
    @DisplayName("generics")
    inner class Generics {

        @Test
        fun `works with integers`() {
            val list = CircularLinkedList<Int>()
            listOf(1, 2, 3).forEach { list.addLast(it) }

            assertEquals(listOf(1, 2, 3), list.toList())
            assertEquals(2, list.next())
        }

        @Test
        fun `works with data classes and stores the very same instance`() {
            val first = Checkpoint(1, "Entrance")
            val list = CircularLinkedList<Checkpoint>()

            list.addLast(first)

            assertSame(first, list.current())
        }

        @Test
        fun `does not deduplicate equal instances`() {
            val checkpoint = Checkpoint(1, "Entrance")
            val list = CircularLinkedList<Checkpoint>()

            list.addLast(checkpoint)
            list.addLast(checkpoint)

            assertEquals(2, list.size)
        }
    }
}
