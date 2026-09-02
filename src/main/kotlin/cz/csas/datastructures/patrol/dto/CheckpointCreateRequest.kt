package cz.csas.datastructures.patrol.dto
import cz.csas.datastructures.patrol.model.Priority

data class CheckpointCreateRequest(
    val name: String,
    val description: String,
    val priority: Priority,
)