package cz.csas.datastructures.patrol.dto

data class PatrolStateResponse(
    val current: CheckpointResponse?,
    val checkpoints: List<CheckpointResponse>,
)
