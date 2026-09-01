package cz.csas.datastructures.patrol.datastructure

import cz.csas.datastructures.patrol.support.LinkedListProbe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * These tests look inside the list with reflection.
 *
 * The public behaviour can be faked with a ready made collection, so this is where
 * the assignment is really enforced: the elements have to live in your own nodes,
 * the chain has to be closed in both directions and the three anchors first, last
 * and current have to point where they are supposed to.
 */
@DisplayName("CircularLinkedList - internal structure")
class CircularLinkedListStructureTest {

    private fun listOfNames(vararg names: String): CircularLinkedList<String> =
        CircularLinkedList<String>().apply { names.forEach { addLast(it) } }

    @Nested
    @DisplayName("storage")
    inner class Storage {

        @Test
        fun `an empty list uses no ready made collection`() {
            LinkedListProbe(CircularLinkedList<String>()).assertNoCollectionStorage()
        }

        @Test
        fun `a filled list uses no ready made collection`() {
            val list = listOfNames("Entrance", "Shelf A", "Charging", "Dispatch")

            LinkedListProbe(list).assertNoCollectionStorage()
        }

        @Test
        fun `the nodes use no ready made collection either`() {
            val list = listOfNames("Entrance", "Shelf A")

            LinkedListProbe(list).assertNoCollectionStorage()
        }
    }

    @Nested
    @DisplayName("anchors")
    inner class Anchors {

        @Test
        fun `an empty list has first, last and current all null`() {
            val probe = LinkedListProbe(CircularLinkedList<String>())

            assertNull(probe.firstNode)
            assertNull(probe.lastNode)
            assertNull(probe.currentNode)
        }

        @Test
        fun `a single item is first, last and current at the same time`() {
            val probe = LinkedListProbe(listOfNames("Entrance"))

            val node = assertNotNull(probe.firstNode)
            assertSame(node, probe.lastNode)
            assertSame(node, probe.currentNode)
            assertSame(node, probe.nextOf(node))
            assertSame(node, probe.previousOf(node))
        }

        @Test
        fun `first holds the first added item and last holds the last one`() {
            val probe = LinkedListProbe(listOfNames("Entrance", "Shelf A", "Dispatch"))

            assertEquals("Entrance", probe.dataOf(assertNotNull(probe.firstNode)))
            assertEquals("Dispatch", probe.dataOf(assertNotNull(probe.lastNode)))
        }

        @Test
        fun `emptying the list resets all three anchors back to null`() {
            val list = listOfNames("Entrance", "Shelf A")
            val probe = LinkedListProbe(list)

            list.removeCurrent()
            list.removeCurrent()

            assertNull(probe.firstNode)
            assertNull(probe.lastNode)
            assertNull(probe.currentNode)
        }

        @Test
        fun `current follows the cursor movements`() {
            val list = listOfNames("Entrance", "Shelf A", "Dispatch")
            val probe = LinkedListProbe(list)

            list.next()
            assertEquals("Shelf A", probe.dataOf(assertNotNull(probe.currentNode)))

            list.previous()
            assertEquals("Entrance", probe.dataOf(assertNotNull(probe.currentNode)))

            list.previous()
            assertEquals("Dispatch", probe.dataOf(assertNotNull(probe.currentNode)))
        }
    }

    @Nested
    @DisplayName("the chain is circular and doubly linked")
    inner class Chain {

        @Test
        fun `walking next from first comes back to first`() {
            val probe = LinkedListProbe(listOfNames("A", "B", "C", "D"))

            assertEquals(listOf("A", "B", "C", "D"), probe.forwardData())
        }

        @Test
        fun `walking previous from last comes back to last`() {
            val probe = LinkedListProbe(listOfNames("A", "B", "C", "D"))

            assertEquals(listOf("D", "C", "B", "A"), probe.backwardData())
        }

        @Test
        fun `last next is first and first previous is last`() {
            val probe = LinkedListProbe(listOfNames("A", "B", "C"))

            assertSame(probe.firstNode, probe.nextOf(assertNotNull(probe.lastNode)))
            assertSame(probe.lastNode, probe.previousOf(assertNotNull(probe.firstNode)))
        }

        @Test
        fun `every link is consistent in both directions`() {
            val list = listOfNames("A", "B", "C", "D", "E")

            LinkedListProbe(list).assertStructuralInvariants(list.size, list.current())
        }

        @Test
        fun `invariants hold after addLast`() {
            val list = CircularLinkedList<String>()
            val probe = LinkedListProbe(list)

            listOf("A", "B", "C", "D").forEach { name ->
                list.addLast(name)
                probe.assertStructuralInvariants(list.size, list.current())
            }
        }

        @Test
        fun `invariants hold after addAfterCurrent`() {
            val list = listOfNames("A", "B", "C")
            val probe = LinkedListProbe(list)

            list.addAfterCurrent("X")
            probe.assertStructuralInvariants(list.size, list.current())

            list.next()
            list.next()
            list.addAfterCurrent("Y")
            probe.assertStructuralInvariants(list.size, list.current())

            assertEquals(listOf("A", "X", "B", "Y", "C"), list.toList())
        }

        @Test
        fun `invariants hold after appending behind the last item`() {
            val list = listOfNames("A", "B")
            val probe = LinkedListProbe(list)

            list.next()
            list.addAfterCurrent("C")

            probe.assertStructuralInvariants(list.size, list.current())
            assertEquals("C", probe.dataOf(assertNotNull(probe.lastNode)))
        }

        @Test
        fun `invariants hold after every removal`() {
            val list = listOfNames("A", "B", "C", "D", "E")
            val probe = LinkedListProbe(list)

            while (!list.isEmpty()) {
                list.removeCurrent()
                probe.assertStructuralInvariants(list.size, if (list.isEmpty()) null else list.current())
            }
        }

        @Test
        fun `invariants hold after removing the first item`() {
            val list = listOfNames("A", "B", "C")
            val probe = LinkedListProbe(list)

            list.removeCurrent()

            probe.assertStructuralInvariants(list.size, list.current())
            assertEquals("B", probe.dataOf(assertNotNull(probe.firstNode)))
            assertEquals("C", probe.dataOf(assertNotNull(probe.lastNode)))
        }

        @Test
        fun `invariants hold after removing the last item`() {
            val list = listOfNames("A", "B", "C")
            val probe = LinkedListProbe(list)

            list.previous()
            list.removeCurrent()

            probe.assertStructuralInvariants(list.size, list.current())
            assertEquals("A", probe.dataOf(assertNotNull(probe.firstNode)))
            assertEquals("B", probe.dataOf(assertNotNull(probe.lastNode)))
        }

        @Test
        fun `a removed node is unlinked from the chain`() {
            val list = listOfNames("A", "B", "C")
            val probe = LinkedListProbe(list)
            list.next()
            val doomed = assertNotNull(probe.currentNode)

            list.removeCurrent()

            probe.assertUnreachable(doomed)
        }

        @Test
        fun `invariants hold after the list has been emptied and refilled`() {
            val list = listOfNames("A", "B")
            val probe = LinkedListProbe(list)

            list.removeCurrent()
            list.removeCurrent()
            list.addLast("X")
            list.addLast("Y")

            probe.assertStructuralInvariants(list.size, list.current())
            assertEquals(listOf("X", "Y"), probe.forwardData())
            assertEquals(listOf("Y", "X"), probe.backwardData())
        }

        @Test
        fun `the iterator order matches the physical chain order`() {
            val list = listOfNames("A", "B", "C", "D")
            val probe = LinkedListProbe(list)

            list.next()
            list.addAfterCurrent("X")
            list.previous()

            assertEquals(probe.forwardData(), list.toList())
        }
    }
}
