package cz.csas.datastructures.patrol.model

data class PatrolState(
    val current: Checkpoint?,
    val checkpoints: List<Checkpoint>,
)
