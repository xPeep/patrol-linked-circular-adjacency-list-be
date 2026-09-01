package cz.csas.datastructures.patrol.dto

import cz.csas.datastructures.patrol.model.Priority
import java.util.UUID

/** One checkpoint as the frontend sees it. Node references are never exposed. */
data class CheckpointResponse(
    val id: UUID,
    val name: String,
    val description: String,
    val priority: Priority,
)
