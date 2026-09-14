package cz.csas.datastructures.patrol.datastructure

class CircularLinkedList<T : Any> : CircularList<T> {

    var head: Node<T>? = null
    var tail: Node<T>? = null
    var current: Node<T>? = null
    var modCount: Int = 0

    data class Node<T>(
        var data: T,
        var next: Node<T>? = null,
        var prev: Node<T>? = null
    )

    override val size: Int
        get() {
            if (head == null) return 0
            var count = 1
            var current = head!!.next
            while (current !== head) {
                count++
                current = current!!.next
            }
            return count
        }

    override fun isEmpty(): Boolean = head == null

    override fun addLast(item: T){
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
        modCount++
    }

    override fun addAfterCurrent(item: T) {
        val newNode = Node(item)
        val nodeAfterCurrent = current?.next
        if (head == null) {
            newNode.next = newNode
            newNode.prev = newNode
            head = newNode
            tail = newNode
            current = head
        } else {
            newNode.next = nodeAfterCurrent
            nodeAfterCurrent?.prev = newNode
            newNode.prev = current
            current?.next = newNode
        }
        if (current === tail) {
            tail = newNode
        }
        modCount++
    }

    override fun current(): T {
        current ?: throw NoSuchElementException()
        return current!!.data
    }

    override fun next(): T {
        val node = current ?: throw NoSuchElementException("Cannot move to next checkpoint because patrol route is empty")
        current = node.next
        return current!!.data
    }

    override fun previous(): T {
        val node = current ?: throw NoSuchElementException("Cannot move to previous checkpoint because patrol route is empty")
        current = node.prev
        return current!!.data
    }

    override fun removeCurrent(): T {
        val nodeToRemove = current ?: throw NoSuchElementException("List is empty")
        val removedData = nodeToRemove.data
        val nodeBeforeCurrent = nodeToRemove.prev
        val nodeAfterCurrent = nodeToRemove.next
        if (nodeAfterCurrent === nodeToRemove) {
            head = null
            tail = null
            current = null
        } else {
            if (nodeToRemove === head) head = nodeAfterCurrent
            if (nodeToRemove === tail) tail = nodeBeforeCurrent
            nodeBeforeCurrent?.next = nodeAfterCurrent
            nodeAfterCurrent?.prev = nodeBeforeCurrent
            current = nodeAfterCurrent
        }
        modCount++
        return removedData
    }

    override fun iterator(): Iterator<T> {
        var nextNode = head
        var index = 0
        val expectedModCount = modCount
        return object : Iterator<T> {
            override fun hasNext(): Boolean = index < size
            override fun next(): T {
                if (hasNext()) {
                    val value = nextNode!!.data
                    nextNode = nextNode?.next
                    index++
                    if (modCount != expectedModCount) throw ConcurrentModificationException()
                    return value
                } else throw NoSuchElementException()
            }
        }
    }

    override fun allCheckpoints(): CheckpointList {
        val result = CheckpointList(size)
        return result
    }

    inner class CheckpointList(private val count: Int) : AbstractList<T>() {
        override val size: Int
            get() = count

        override fun get(index: Int): T {
            var node = head
            repeat(index) {
                node = node?.next
            }
            return node!!.data
        }
    }
}
