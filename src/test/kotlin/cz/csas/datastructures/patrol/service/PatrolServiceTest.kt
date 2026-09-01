package cz.csas.datastructures.patrol.service

import cz.csas.datastructures.patrol.model.Priority
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("PatrolService")
class PatrolServiceTest {

    private val service = PatrolService()

    /** Adds a single checkpoint behind the current one. */
    private fun add(name: String, priority: Priority = Priority.NORMAL) =
        service.addCheckpoint(name, "description of $name", priority)

    /**
     * Builds a route in the given order the way an operator would: add a checkpoint,
     * step onto it, add the next one. The cursor ends up on the first checkpoint.
     */
    private fun buildRoute(vararg names: String) {
        names.forEach {
            add(it)
            service.moveNext()
        }
        if (names.isNotEmpty()) service.moveNext()
    }

    private fun names() = service.state().checkpoints.map { it.name }

    private fun currentName() = service.state().current?.name

    @Nested
    @DisplayName("an empty route")
    inner class EmptyRoute {

        @Test
        fun `starts with no current checkpoint and an empty list`() {
            val state = service.state()

            assertNull(state.current)
            assertTrue(state.checkpoints.isEmpty())
        }

        @Test
        fun `moving next raises a domain error instead of crashing`() {
            val failure = assertFailsWith<PatrolEmptyException> { service.moveNext() }

            assertEquals("Cannot move to next checkpoint because patrol route is empty", failure.message)
        }

        @Test
        fun `moving previous raises a domain error instead of crashing`() {
            val failure = assertFailsWith<PatrolEmptyException> { service.movePrevious() }

            assertEquals("Cannot move to previous checkpoint because patrol route is empty", failure.message)
        }

        @Test
        fun `removing raises a domain error instead of crashing`() {
            val failure = assertFailsWith<PatrolEmptyException> { service.removeCurrentCheckpoint() }

            assertEquals("Cannot remove current checkpoint because patrol route is empty", failure.message)
        }

        @Test
        fun `stays readable after a failed operation`() {
            runCatching { service.moveNext() }
            runCatching { service.movePrevious() }
            runCatching { service.removeCurrentCheckpoint() }

            val state = service.state()
            assertNull(state.current)
            assertTrue(state.checkpoints.isEmpty())
        }
    }

    @Nested
    @DisplayName("adding checkpoints")
    inner class Adding {

        @Test
        fun `the first checkpoint becomes the current one`() {
            val state = add("Entrance")

            assertEquals("Entrance", state.current?.name)
            assertEquals(listOf("Entrance"), state.checkpoints.map { it.name })
        }

        @Test
        fun `every checkpoint gets a server generated id`() {
            val state = add("Entrance")
            val checkpoint = assertNotNull(state.current)

            assertEquals(checkpoint.id, state.checkpoints.single().id)
        }

        @Test
        fun `ids are unique`() {
            buildRoute("Entrance", "Shelf A", "Charging")

            val ids = service.state().checkpoints.map { it.id }
            assertEquals(ids.size, ids.toSet().size)
        }

        @Test
        fun `two checkpoints with identical data still get different ids`() {
            add("Entrance")
            add("Entrance")

            val ids = service.state().checkpoints.map { it.id }
            assertNotEquals(ids[0], ids[1])
        }

        @Test
        fun `a new checkpoint is inserted behind the current one`() {
            add("Entrance")
            add("Shelf A")

            assertEquals(listOf("Entrance", "Shelf A"), names())
            assertEquals("Entrance", currentName())
        }

        @Test
        fun `adding never moves the robot`() {
            add("Entrance")
            add("Shelf A")
            service.moveNext()
            assertEquals("Shelf A", currentName())

            add("Charging")

            assertEquals("Shelf A", currentName())
            assertEquals(listOf("Entrance", "Shelf A", "Charging"), names())
        }

        @Test
        fun `the priority is stored as given`() {
            add("Entrance", Priority.HIGH)

            assertEquals(Priority.HIGH, service.state().current?.priority)
        }

        @Test
        fun `surrounding whitespace is trimmed away`() {
            val state = service.addCheckpoint("  Entrance  ", "  Main gate  ", Priority.LOW)

            assertEquals("Entrance", state.current?.name)
            assertEquals("Main gate", state.current?.description)
        }

        @Test
        fun `the returned state is the complete new state`() {
            add("Entrance")
            val state = add("Shelf A")

            assertEquals(2, state.checkpoints.size)
            assertEquals("Entrance", state.current?.name)
        }
    }

    @Nested
    @DisplayName("moving the robot")
    inner class Moving {

        @Test
        fun `next walks forward through the route`() {
            buildRoute("Entrance", "Shelf A", "Charging")
            assertEquals("Entrance", currentName())

            assertEquals("Shelf A", service.moveNext().current?.name)
            assertEquals("Charging", service.moveNext().current?.name)
            assertEquals("Entrance", service.moveNext().current?.name)
        }

        @Test
        fun `next wraps around after the last checkpoint`() {
            buildRoute("Entrance", "Shelf A")

            assertEquals("Shelf A", service.moveNext().current?.name)
            assertEquals("Entrance", service.moveNext().current?.name)
        }

        @Test
        fun `previous wraps around before the first checkpoint`() {
            buildRoute("Entrance", "Shelf A")
            assertEquals("Entrance", currentName())

            assertEquals("Shelf A", service.movePrevious().current?.name)
        }

        @Test
        fun `previous undoes next`() {
            buildRoute("Entrance", "Shelf A", "Charging")

            service.moveNext()
            service.movePrevious()

            assertEquals("Entrance", currentName())
        }

        @Test
        fun `moving never changes the route itself`() {
            buildRoute("Entrance", "Shelf A", "Charging")
            val before = names()

            repeat(10) { service.moveNext() }
            repeat(10) { service.movePrevious() }

            assertEquals(before, names())
            assertEquals("Entrance", currentName())
        }

        @Test
        fun `a one checkpoint route always stays on the same checkpoint`() {
            add("Entrance")

            assertEquals("Entrance", service.moveNext().current?.name)
            assertEquals("Entrance", service.movePrevious().current?.name)
        }

        @Test
        fun `the returned state is the complete new state`() {
            buildRoute("Entrance", "Shelf A")

            val state = service.moveNext()

            assertEquals(2, state.checkpoints.size)
            assertEquals(listOf("Entrance", "Shelf A"), state.checkpoints.map { it.name })
        }
    }

    @Nested
    @DisplayName("removing checkpoints")
    inner class Removing {

        @Test
        fun `the robot moves onto the successor`() {
            buildRoute("Entrance", "Shelf A", "Charging")

            val state = service.removeCurrentCheckpoint()

            assertEquals("Shelf A", state.current?.name)
            assertEquals(listOf("Shelf A", "Charging"), state.checkpoints.map { it.name })
        }

        @Test
        fun `removing the last checkpoint wraps the robot to the first one`() {
            buildRoute("Entrance", "Shelf A", "Charging")
            service.movePrevious()
            assertEquals("Charging", currentName())

            val state = service.removeCurrentCheckpoint()

            assertEquals("Entrance", state.current?.name)
            assertEquals(listOf("Entrance", "Shelf A"), state.checkpoints.map { it.name })
        }

        @Test
        fun `removing the only checkpoint empties the route`() {
            add("Entrance")

            val state = service.removeCurrentCheckpoint()

            assertNull(state.current)
            assertTrue(state.checkpoints.isEmpty())
        }

        @Test
        fun `an emptied route can be filled again`() {
            add("Entrance")
            service.removeCurrentCheckpoint()

            val state = add("Fresh start")

            assertEquals("Fresh start", state.current?.name)
            assertEquals(1, state.checkpoints.size)
        }

        @Test
        fun `an emptied route reports the domain error again`() {
            add("Entrance")
            service.removeCurrentCheckpoint()

            assertFailsWith<PatrolEmptyException> { service.moveNext() }
            assertFailsWith<PatrolEmptyException> { service.movePrevious() }
            assertFailsWith<PatrolEmptyException> { service.removeCurrentCheckpoint() }
        }

        @Test
        fun `removing works through the whole route`() {
            buildRoute("Entrance", "Shelf A", "Charging", "Dispatch")

            val removedInOrder = (1..4).map {
                val current = currentName()
                service.removeCurrentCheckpoint()
                current
            }

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), removedInOrder)
            assertTrue(service.state().checkpoints.isEmpty())
        }
    }

    @Nested
    @DisplayName("the state snapshot")
    inner class StateSnapshot {

        @Test
        fun `lists the checkpoints from first to last, not from current`() {
            buildRoute("Entrance", "Shelf A", "Charging")
            service.moveNext()
            service.moveNext()
            assertEquals("Charging", currentName())

            assertEquals(listOf("Entrance", "Shelf A", "Charging"), names())
        }

        @Test
        fun `the current checkpoint is one of the listed checkpoints`() {
            buildRoute("Entrance", "Shelf A")
            service.moveNext()

            val state = service.state()
            val current = assertNotNull(state.current)
            assertTrue(state.checkpoints.any { it.id == current.id })
        }

        @Test
        fun `a snapshot is not affected by later changes`() {
            add("Entrance")
            val snapshot = service.state()

            add("Shelf A")

            assertEquals(1, snapshot.checkpoints.size)
        }

        @Test
        fun `reading the state does not change anything`() {
            buildRoute("Entrance", "Shelf A")

            repeat(5) { service.state() }

            assertEquals("Entrance", currentName())
            assertEquals(listOf("Entrance", "Shelf A"), names())
        }
    }

    @Nested
    @DisplayName("a realistic operator session")
    inner class OperatorSession {

        @Test
        fun `builds, walks and edits a route`() {
            add("Entrance")
            service.moveNext()
            add("Shelf A")
            service.moveNext()
            add("Charging")
            service.moveNext()
            add("Dispatch")

            assertEquals(listOf("Entrance", "Shelf A", "Charging", "Dispatch"), names())
            assertEquals("Charging", currentName())

            service.removeCurrentCheckpoint()
            assertEquals(listOf("Entrance", "Shelf A", "Dispatch"), names())
            assertEquals("Dispatch", currentName())

            service.moveNext()
            assertEquals("Entrance", currentName())
        }
    }
}
