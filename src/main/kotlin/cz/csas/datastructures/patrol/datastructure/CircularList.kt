package cz.csas.datastructures.patrol.datastructure

/**
 * A circular, doubly linked list with a movable cursor called `current`.
 *
 * The list keeps three logical anchors:
 *  * `first`  – the head of the list, the element returned first by [iterator],
 *  * `last`   – the tail of the list; `last.next` is always `first`,
 *  * `current`– the cursor the patrol robot is standing on.
 *
 * Implementations must store the elements in their own linked nodes.
 * Using [java.util.Collection], [java.util.Map] or arrays as the element
 * storage is forbidden.
 */
interface CircularList<T : Any> : Iterable<T> {

    val size: Int

    fun isEmpty(): Boolean

    fun addLast(item: T)

    fun addAfterCurrent(item: T)

    fun current(): T

    fun next(): T

    fun previous(): T

    fun removeCurrent(): T

    override fun iterator(): Iterator<T>

    fun allCheckpoints(): List<T>
}
