package cz.csas.datastructures.patrol.support

/**
 * Dead simple reference implementation used only by the randomized tests.
 *
 * It is allowed to use a ready made collection because it lives in the test source
 * set and is never the thing under test. Its whole purpose is to say what the
 * hand written [cz.csas.datastructures.patrol.datastructure.CircularLinkedList]
 * should have done.
 */
class ReferenceCircularList<T : Any> {

    private val items = ArrayList<T>()
    private var cursor = -1

    val size: Int get() = items.size

    fun isEmpty(): Boolean = items.isEmpty()

    fun addLast(item: T) {
        items.add(item)
        if (cursor == -1) cursor = 0
    }

    fun addAfterCurrent(item: T) {
        if (items.isEmpty()) {
            items.add(item)
            cursor = 0
            return
        }
        items.add(cursor + 1, item)
    }

    fun current(): T {
        require(items.isNotEmpty()) { "empty" }
        return items[cursor]
    }

    fun next(): T {
        require(items.isNotEmpty()) { "empty" }
        cursor = (cursor + 1) % items.size
        return items[cursor]
    }

    fun previous(): T {
        require(items.isNotEmpty()) { "empty" }
        cursor = (cursor - 1 + items.size) % items.size
        return items[cursor]
    }

    fun removeCurrent(): T {
        require(items.isNotEmpty()) { "empty" }
        val removed = items.removeAt(cursor)
        cursor = when {
            items.isEmpty() -> -1
            cursor >= items.size -> 0
            else -> cursor
        }
        return removed
    }

    fun toList(): List<T> = ArrayList(items)
}
