package cz.csas.datastructures.patrol.datastructure

import cz.csas.datastructures.patrol.support.LinkedListProbe
import cz.csas.datastructures.patrol.support.ReferenceCircularList
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Model based testing.
 *
 * Thousands of random operations are executed against the hand written list and
 * against a trivial reference implementation at the same time. After every single
 * operation both must agree on the size, on the current element and on the whole
 * content, and the internal chain must still be a valid circular doubly linked list.
 *
 * When a test in this class fails, the printed operation log is the exact recipe
 * needed to reproduce the bug.
 */
@DisplayName("CircularLinkedList - randomized model based checks")
class CircularLinkedListRandomizedTest {

    private enum class Operation { ADD_LAST, ADD_AFTER_CURRENT, NEXT, PREVIOUS, REMOVE_CURRENT }

    @ParameterizedTest(name = "seed {0}")
    @ValueSource(longs = [1L, 2L, 3L, 42L, 1234L, 987654321L])
    fun `behaves exactly like the reference implementation`(seed: Long) {
        val random = Random(seed)
        val actual = CircularLinkedList<String>()
        val expected = ReferenceCircularList<String>()
        val probe = LinkedListProbe(actual)
        val log = ArrayList<String>()
        var counter = 0

        try {
            repeat(1_500) {
                val operation = Operation.entries[random.nextInt(Operation.entries.size)]
                log.add(operation.name)

                when (operation) {
                    Operation.ADD_LAST -> {
                        val item = "item-" + counter++
                        actual.addLast(item)
                        expected.addLast(item)
                    }

                    Operation.ADD_AFTER_CURRENT -> {
                        val item = "item-" + counter++
                        actual.addAfterCurrent(item)
                        expected.addAfterCurrent(item)
                    }

                    Operation.NEXT -> if (expected.isEmpty()) {
                        assertFailsWith<NoSuchElementException> { actual.next() }
                    } else {
                        assertEquals(expected.next(), actual.next(), "next() returned the wrong element")
                    }

                    Operation.PREVIOUS -> if (expected.isEmpty()) {
                        assertFailsWith<NoSuchElementException> { actual.previous() }
                    } else {
                        assertEquals(expected.previous(), actual.previous(), "previous() returned the wrong element")
                    }

                    Operation.REMOVE_CURRENT -> if (expected.isEmpty()) {
                        assertFailsWith<NoSuchElementException> { actual.removeCurrent() }
                    } else {
                        assertEquals(
                            expected.removeCurrent(),
                            actual.removeCurrent(),
                            "removeCurrent() returned the wrong element",
                        )
                    }
                }

                assertEquals(expected.size, actual.size, "wrong size")
                assertEquals(expected.isEmpty(), actual.isEmpty(), "wrong isEmpty()")
                assertEquals(expected.toList(), actual.toList(), "wrong content or wrong order")
                if (expected.isEmpty()) {
                    assertFailsWith<NoSuchElementException> { actual.current() }
                } else {
                    assertEquals(expected.current(), actual.current(), "wrong current element")
                }

                probe.assertStructuralInvariants(
                    expectedSize = expected.size,
                    expectedCurrent = if (expected.isEmpty()) null else expected.current(),
                )
            }
        } catch (failure: Throwable) {
            throw AssertionError(
                "Failed with seed " + seed + " after " + log.size + " operations.\n" +
                    "Operation log (last 60):\n" +
                    log.takeLast(60).joinToString("\n") + "\n" +
                    "Cause: " + failure.message,
                failure,
            )
        }
    }

    @Test
    fun `a route that is repeatedly emptied and refilled stays correct`() {
        val random = Random(7)
        val actual = CircularLinkedList<Int>()
        val expected = ReferenceCircularList<Int>()

        repeat(200) { round ->
            repeat(random.nextInt(1, 8)) {
                actual.addLast(round * 100 + it)
                expected.addLast(round * 100 + it)
            }
            repeat(random.nextInt(0, 5)) {
                actual.next()
                expected.next()
            }
            while (!expected.isEmpty()) {
                assertEquals(expected.removeCurrent(), actual.removeCurrent())
                assertEquals(expected.toList(), actual.toList())
            }
            assertEquals(0, actual.size)
        }
    }

    @Test
    fun `ten thousand appends keep the list consistent`() {
        val list = CircularLinkedList<Int>()
        val probe = LinkedListProbe(list)

        repeat(10_000) { list.addLast(it) }

        assertEquals(10_000, list.size)
        assertEquals((0 until 10_000).toList(), list.toList())
        assertEquals(0, list.current())
        probe.assertStructuralInvariants(10_000, 0)
    }

    @Test
    fun `walking a large route forward and backward returns to the start`() {
        val list = CircularLinkedList<Int>()
        repeat(1_000) { list.addLast(it) }

        repeat(2_500) { list.next() }
        repeat(2_500) { list.previous() }

        assertEquals(0, list.current())
    }
}
