package cz.csas.datastructures.patrol.service

import cz.csas.datastructures.patrol.datastructure.CircularLinkedList
import cz.csas.datastructures.patrol.model.Checkpoint
import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.model.Priority
import org.springframework.stereotype.Service

@Service
class PatrolService {

    private val checkpoints = CircularLinkedList<Checkpoint>()
    init {
        checkpoints.addLast(Checkpoint(name = "debil", description = "pffff", priority = Priority.LOW ))
        checkpoints.addLast(Checkpoint(name = "debil2", description = "brrrr", priority = Priority.HIGH))
        checkpoints.addLast(Checkpoint(name = "debil3", description =  "skrrrr", priority =  Priority.LOW))
        checkpoints.addLast(Checkpoint(name = "debil4", description =  "skrrrr", priority =  Priority.LOW))
        checkpoints.addLast(Checkpoint(name = "debil5", description =  "skrrrr", priority =  Priority.LOW))
        checkpoints.addLast(Checkpoint(name = "debil6", description =  "skrrrr", priority =  Priority.LOW))
    }

    fun state(): PatrolState =
        PatrolState(
            current = currentCheckpoint(),
            checkpoints = allCheckpoints()
        )
    fun currentCheckpoint(): Checkpoint? =
        checkpoints.current()

    fun addCheckpoint(name: String, description: String, priority: Priority): PatrolState =
        TODO("Create the checkpoint, add it behind current, return the new state")

    fun moveNext(): PatrolState {
        checkpoints.next()
        return state()
    }

    fun movePrevious(): PatrolState {
        checkpoints.previous()
        return state()
    }
    fun removeCurrentCheckpoint(): PatrolState = TODO("Remove the current checkpoint or raise PatrolEmptyException")

    fun allCheckpoints(): List<Checkpoint> {
        return checkpoints.allCheckpoints()
    }
}
