package cz.csas.datastructures.patrol.service

import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.model.Priority
import org.springframework.stereotype.Service

/**
 * TODO: keeps the patrol route in memory and translates application operations
 * into operations on your own [cz.csas.datastructures.patrol.datastructure.CircularList].
 *
 * There is no database and no repository layer on purpose. The route must live in a
 * property of this service, held by your custom list - the tests check by reflection
 * that the service really owns a `CircularList` and no ready made collection.
 *
 * Rules:
 *  * a new checkpoint gets a server generated id and is inserted behind the current one,
 *  * the very first checkpoint of an empty route becomes the current one,
 *  * an operation that needs a current checkpoint on an empty route must raise
 *    [PatrolEmptyException] with the exact message documented in the README,
 *    never let a [NoSuchElementException] escape to the web layer.
 */
@Service
class PatrolService {

    /** Current state of the whole route. An empty route is a valid state, never an error. */
    fun state(): PatrolState = TODO("Build the snapshot: current checkpoint plus all checkpoints from first to last")

    /** Creates a checkpoint with a fresh id and inserts it behind the current one. */
    fun addCheckpoint(name: String, description: String, priority: Priority): PatrolState =
        TODO("Create the checkpoint, add it behind current, return the new state")

    /** Moves the robot one checkpoint forward, wrapping around after the last one. */
    fun moveNext(): PatrolState = TODO("Move forward or raise PatrolEmptyException")

    /** Moves the robot one checkpoint backward, wrapping around before the first one. */
    fun movePrevious(): PatrolState = TODO("Move backward or raise PatrolEmptyException")

    /** Removes the current checkpoint; the robot moves onto its successor. */
    fun removeCurrentCheckpoint(): PatrolState = TODO("Remove the current checkpoint or raise PatrolEmptyException")
}
