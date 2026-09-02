package cz.csas.datastructures.patrol.service

/** Raised when an operation needs a current checkpoint but the route is empty. */
class PatrolEmptyException(message: String) : RuntimeException(message)
class InputEmptyException(message: String): RuntimeException(message)