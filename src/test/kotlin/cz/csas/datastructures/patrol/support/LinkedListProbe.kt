package cz.csas.datastructures.patrol.support

import cz.csas.datastructures.patrol.datastructure.CircularList
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Reflection based X-ray of a hand written linked list.
 *
 * Behavioural tests alone can be satisfied by an implementation that secretly keeps
 * an ArrayList inside. This probe looks at the real object graph instead and lets
 * the tests assert that the structure truly is a circular doubly linked list.
 *
 * Required naming (as stated in the assignment):
 *  - list fields: first, last, current
 *  - node fields: data, next, previous
 */
class LinkedListProbe(private val list: CircularList<*>) {

    companion object {
        private val FIRST_NAMES = listOf("first", "head")
        private val LAST_NAMES = listOf("last", "tail")
        private val CURRENT_NAMES = listOf("current", "cursor")
        private val DATA_NAMES = listOf("data", "value", "item", "element", "payload")
        private val NEXT_NAMES = listOf("next", "nextNode")
        private val PREVIOUS_NAMES = listOf("previous", "prev", "previousNode")

        private val FORBIDDEN_STORAGE_TYPES = listOf(
            java.util.Collection::class.java,
            java.util.Map::class.java,
            java.util.Iterator::class.java,
        )
    }

    private val listFields: List<Field> = declaredFieldsOf(list.javaClass)

    val firstNode: Any? get() = readListField(FIRST_NAMES, "first")
    val lastNode: Any? get() = readListField(LAST_NAMES, "last")
    val currentNode: Any? get() = readListField(CURRENT_NAMES, "current")

    /** Walks next from first, at most [limit] steps, stopping when it comes back to first. */
    fun forwardChain(limit: Int = 100_000): List<Any> {
        val start = firstNode ?: return emptyList()
        val nodes = ArrayList<Any>()
        var node: Any? = start
        var steps = 0
        while (node != null && steps <= limit) {
            nodes.add(node)
            node = nextOf(node)
            steps++
            if (node === start) return nodes
        }
        throw AssertionError(
            "Walking next from first never came back to first after " + limit +
                " steps. The list is not circular, or last.next is null."
        )
    }

    /** Walks previous from last, at most [limit] steps, stopping when it comes back to last. */
    fun backwardChain(limit: Int = 100_000): List<Any> {
        val start = lastNode ?: return emptyList()
        val nodes = ArrayList<Any>()
        var node: Any? = start
        var steps = 0
        while (node != null && steps <= limit) {
            nodes.add(node)
            node = previousOf(node)
            steps++
            if (node === start) return nodes
        }
        throw AssertionError(
            "Walking previous from last never came back to last after " + limit +
                " steps. The list is not a circular doubly linked list."
        )
    }

    fun forwardData(): List<Any?> = forwardChain().map { dataOf(it) }

    fun backwardData(): List<Any?> = backwardChain().map { dataOf(it) }

    fun dataOf(node: Any): Any? = readNodeField(node, DATA_NAMES, "data")

    fun nextOf(node: Any): Any? = readNodeField(node, NEXT_NAMES, "next")

    fun previousOf(node: Any): Any? = readNodeField(node, PREVIOUS_NAMES, "previous")

    /**
     * Fails when the list itself, or any of its nodes, stores elements in a ready made
     * collection, map, iterator or array instead of in its own links.
     */
    fun assertNoCollectionStorage() {
        assertNoCollectionFields(listFields, "CircularLinkedList")
        val nodeClass = (firstNode ?: currentNode ?: lastNode)?.javaClass ?: return
        assertNoCollectionFields(declaredFieldsOf(nodeClass), "node class " + nodeClass.simpleName)
    }

    /** Every structural invariant that must hold after any operation. */
    fun assertStructuralInvariants(expectedSize: Int, expectedCurrent: Any?) {
        if (expectedSize == 0) {
            require(firstNode == null) { "first must be null on an empty list but was " + firstNode }
            require(lastNode == null) { "last must be null on an empty list but was " + lastNode }
            require(currentNode == null) { "current must be null on an empty list but was " + currentNode }
            return
        }

        val forward = forwardChain()
        val backward = backwardChain()

        require(forward.size == expectedSize) {
            "Walking next from first visited " + forward.size + " nodes but the list reports size " + expectedSize
        }
        require(backward.size == expectedSize) {
            "Walking previous from last visited " + backward.size + " nodes but the list reports size " + expectedSize
        }
        require(forward.reversed().map { identity(it) } == backward.map { identity(it) }) {
            "The previous chain is not the mirror image of the next chain"
        }

        forward.forEach { node ->
            val next = nextOf(node) ?: throw AssertionError("next of a linked node must never be null")
            val previous = previousOf(node) ?: throw AssertionError("previous of a linked node must never be null")
            require(previousOf(next) === node) { "next.previous must point back to the node itself" }
            require(nextOf(previous) === node) { "previous.next must point back to the node itself" }
        }

        require(nextOf(lastNode!!) === firstNode) { "last.next must be first" }
        require(previousOf(firstNode!!) === lastNode) { "first.previous must be last" }

        val cursor = currentNode ?: throw AssertionError("current must not be null on a non empty list")
        require(forward.any { it === cursor }) { "current points to a node that is not part of the list" }
        require(dataOf(cursor) == expectedCurrent) {
            "current node holds " + dataOf(cursor) + " but current() returned " + expectedCurrent
        }
    }

    /** Confirms a removed node is no longer part of the chain. */
    fun assertUnreachable(node: Any) {
        val reachable = forwardChain().any { it === node }
        require(!reachable) { "A removed node is still reachable from first" }
    }

    private fun identity(node: Any) = System.identityHashCode(node)

    private fun readListField(candidates: List<String>, label: String): Any? {
        val field = listFields.firstOrNull { it.name in candidates }
            ?: throw AssertionError(
                "The list class " + list.javaClass.simpleName + " has no " + label +
                    " field. Declared fields: " + listFields.map { it.name }
            )
        field.isAccessible = true
        return field.get(list)
    }

    private val nodeFieldCache = HashMap<String, Field>()

    private fun readNodeField(node: Any, candidates: List<String>, label: String): Any? {
        val key = node.javaClass.name + "#" + label
        val field = nodeFieldCache.getOrPut(key) {
            val fields = declaredFieldsOf(node.javaClass)
            val resolved = fields.firstOrNull { it.name in candidates }
                ?: throw AssertionError(
                    "The node class " + node.javaClass.simpleName + " has no " + label +
                        " field. Declared fields: " + fields.map { it.name }
                )
            resolved.isAccessible = true
            resolved
        }
        return field.get(node)
    }

    private fun assertNoCollectionFields(fields: List<Field>, label: String) {
        fields.forEach { field ->
            val type = field.type
            if (type.isArray) {
                throw AssertionError(
                    label + " declares the array field " + field.name + " of type " + type.simpleName +
                        ". The elements must be stored in your own linked nodes."
                )
            }
            FORBIDDEN_STORAGE_TYPES.forEach { forbidden ->
                if (forbidden.isAssignableFrom(type)) {
                    throw AssertionError(
                        label + " declares the field " + field.name + " of type " + type.name +
                            ". Ready made collections must not be used as the element storage."
                    )
                }
            }
        }
    }

    private fun declaredFieldsOf(type: Class<*>): List<Field> {
        val fields = ArrayList<Field>()
        var current: Class<*>? = type
        while (current != null && current != Any::class.java) {
            current.declaredFields
                .filterNot { it.isSynthetic }
                .filterNot { Modifier.isStatic(it.modifiers) }
                .forEach { fields.add(it) }
            current = current.superclass
        }
        return fields
    }
}
