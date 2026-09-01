package cz.csas.datastructures.patrol.model

data class Node<T>(
    var data: T,
    var next: Node<T>? = null,
    var prev: Node<T>? = null
)