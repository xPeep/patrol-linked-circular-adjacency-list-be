package cz.csas.datastructures.patrol.dto

/**
 * The single response shape of every successful endpoint.
 *
 * @param current `null` when the route is empty
 * @param checkpoints always an array, ordered from `first` to `last`, never `null`
 */
data class PatrolStateResponse(
    val current: CheckpointResponse?,
    val checkpoints: List<CheckpointResponse>,
)
