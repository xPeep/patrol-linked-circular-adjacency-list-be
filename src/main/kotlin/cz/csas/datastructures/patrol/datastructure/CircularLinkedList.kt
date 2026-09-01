package cz.csas.datastructures.patrol.datastructure

/**
 * TODO: hand written circular doubly linked list.
 *
 * Read the KDoc of [CircularList] first, it defines what every operation must do.
 *
 * Structural contract the tests verify by reflection over the real object graph,
 * so the naming matters:
 *
 *  * the list keeps three references named `first`, `last` and `current`,
 *    all three are `null` while the list is empty,
 *  * every node exposes `data`, `next` and `previous`,
 *  * `last.next === first` and `first.previous === last` at all times,
 *  * `node.next.previous === node` for every node in the chain,
 *  * a removed node is unlinked and no longer reachable from `first`,
 *  * no ready made collection, map or array is used as the element storage,
 *    not even as a local variable inside a method.
 */
class CircularLinkedList<T : Any> : CircularList<T> {

    override val size: Int
        get() = TODO("Return the number of elements currently held by the list")

    override fun isEmpty(): Boolean = TODO("Report whether the list holds no element at all")

    override fun addLast(item: T): Unit = TODO("Append the item behind last")

    override fun addAfterCurrent(item: T): Unit = TODO("Insert the item directly behind current")

    override fun current(): T = TODO("Return the element the cursor stands on")

    override fun next(): T = TODO("Move the cursor forward and return the new current element")

    override fun previous(): T = TODO("Move the cursor backward and return the new current element")

    override fun removeCurrent(): T = TODO("Unlink the current element, return it, move the cursor onto its successor")

    override fun iterator(): Iterator<T> = TODO("Walk from first to last exactly once, then stop")
}
