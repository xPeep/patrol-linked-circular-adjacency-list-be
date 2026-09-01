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

    /** Number of elements currently held by the list. */
    val size: Int

    /** `true` when the list holds no element at all. */
    fun isEmpty(): Boolean

    /**
     * Appends [item] behind `last`.
     *
     * On an empty list the new element becomes `first`, `last` **and** `current`.
     * On a non-empty list `current` is left untouched.
     */
    fun addLast(item: T)

    /**
     * Inserts [item] directly behind `current`.
     *
     * On an empty list the new element becomes `first`, `last` **and** `current`.
     * When `current` is `last`, the new element becomes the new `last`.
     * `current` never moves.
     */
    fun addAfterCurrent(item: T)

    /**
     * The element the cursor stands on.
     *
     * @throws NoSuchElementException when the list is empty.
     */
    fun current(): T

    /**
     * Moves the cursor one step forward and returns the **new** current element.
     * From `last` the cursor wraps around to `first`.
     *
     * @throws NoSuchElementException when the list is empty.
     */
    fun next(): T

    /**
     * Moves the cursor one step backward and returns the **new** current element.
     * From `first` the cursor wraps around to `last`.
     *
     * @throws NoSuchElementException when the list is empty.
     */
    fun previous(): T

    /**
     * Removes the element the cursor stands on and returns it.
     *
     * The cursor moves to the successor of the removed element; when the removed
     * element was `last`, the cursor ends up on `first`. Removing the only
     * element leaves the list empty.
     *
     * @throws NoSuchElementException when the list is empty.
     */
    fun removeCurrent(): T

    /**
     * Iterates the list exactly once, starting at `first` and ending at `last`,
     * no matter where `current` stands. The iterator is fail-fast: structural
     * modification during iteration raises [ConcurrentModificationException].
     */
    override fun iterator(): Iterator<T>
}
