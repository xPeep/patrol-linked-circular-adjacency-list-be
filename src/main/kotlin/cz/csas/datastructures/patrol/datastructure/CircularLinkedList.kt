package cz.csas.datastructures.patrol.datastructure

import cz.csas.datastructures.patrol.model.Node

class CircularLinkedList<T : Any> : CircularList<T> {
    var head: Node<T>? = null
    var tail: Node<T>? = null
    var current: Node<T>? = null

    override val size: Int
        get() {
            if (head == null) return 0
            var count = 1
            var current = head!!.next
            while (current != head) {
                count++
                current = current!!.next
            }
            return count
        }

    override fun isEmpty(): Boolean = head == null

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

    override fun addAfterCurrent(item: T): Unit {
        val newNode = Node(item)
        val nodeAfterCurrent = current?.next
        if (head == null) {
            newNode.next = newNode
            newNode.prev = newNode
            head = newNode
            tail = newNode
            current = head
        }
        newNode.next = nodeAfterCurrent
        nodeAfterCurrent?.prev = newNode
        newNode.prev = current
        current?.next = newNode
    }

    override fun current(): T = current!!.data

    override fun next(): T {
        current = current?.next
        return current!!.data
    }

    override fun previous(): T {
        current = current?.prev
        return current!!.data
    }

    override fun removeCurrent(): T {
        val nodeBeforeCurrent = current?.prev
        val nodeAfterCurrent = current?.next
        if (current == head) {
            head = nodeAfterCurrent
        }
        nodeBeforeCurrent?.next = nodeAfterCurrent
        nodeAfterCurrent?.prev = nodeBeforeCurrent
        current = nodeAfterCurrent
        return current!!.data
    }

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
