package cz.csas.datastructures.patrol.model

import java.util.UUID

data class Checkpoint(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val priority: Priority,
)
