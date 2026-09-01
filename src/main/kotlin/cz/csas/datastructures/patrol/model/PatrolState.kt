package cz.csas.datastructures.patrol.model

/**
 * Immutable snapshot of the whole patrol route.
 *
 * @param current the checkpoint the robot stands on, `null` for an empty route
 * @param checkpoints all checkpoints ordered from `first` to `last`, exactly one pass
 */
data class PatrolState(
    val current: Checkpoint?,
    val checkpoints: List<Checkpoint>,
)
