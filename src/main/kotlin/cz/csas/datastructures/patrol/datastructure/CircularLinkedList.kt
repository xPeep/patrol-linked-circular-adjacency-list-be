package cz.csas.datastructures.patrol.datastructure

import cz.csas.datastructures.patrol.model.Node

class CircularLinkedList<T : Any> : CircularList<T> {
    var head: Node<T>? = null
    var tail: Node<T>? = null
    var current: Node<T>? = null

    override val size: Int
        get() = TODO("Return the number of elements currently held by the list")

    override fun isEmpty(): Boolean = TODO("Report whether the list holds no element at all")

    override fun addLast(item: T): Unit {
        val newNode = Node(item)
        if (head == null) {
            newNode.next = newNode
            newNode.prev = newNode
            head = newNode
            tail = newNode
            current = head
        } else {
            tail?.next = newNode
            head?.prev = newNode
            newNode.next = head
            newNode.prev = tail
            tail = newNode
        }
    }

    override fun addAfterCurrent(item: T): Unit = TODO("Insert the item directly behind current")

    override fun current(): T = current!!.data

    override fun next(): T {
        current = current?.next
        return current!!.data
    }

    override fun previous(): T {
        current = current?.prev
        return current!!.data
    }

    override fun removeCurrent(): T = TODO("Unlink the current element, return it, move the cursor onto its successor")

    override fun iterator(): Iterator<T> = TODO("Walk from first to last exactly once, then stop")

    override fun allCheckpoints(): List<T> {
        val start = head ?: return emptyList()
        val result = mutableListOf<T>()
        var node = start
        do {
            result.add(node.data)
            node = node.next!!
        } while (node != start)
        return result
    }
}
