package cz.csas.datastructures.patrol.model

import java.util.UUID

data class Checkpoint(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val priority: Priority,
)