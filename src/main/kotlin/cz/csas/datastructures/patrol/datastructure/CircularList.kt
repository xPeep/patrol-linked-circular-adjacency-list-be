package cz.csas.datastructures.patrol.datastructure

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
