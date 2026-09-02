package cz.csas.datastructures.patrol.service

import cz.csas.datastructures.patrol.datastructure.CircularLinkedList
import cz.csas.datastructures.patrol.model.Checkpoint
import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.model.Priority
import org.springframework.stereotype.Service

@Service
class PatrolService {

    private val checkpoints = CircularLinkedList<Checkpoint>()

    fun state(): PatrolState {
        return if (checkpoints.isEmpty()) {
            PatrolState(
                current = null,
                checkpoints = emptyList()
            )
        } else
            PatrolState(
                current = currentCheckpoint(),
                checkpoints = allCheckpoints()
            )
    }

    fun currentCheckpoint(): Checkpoint? =
        checkpoints.current()

    fun createCheckpoint(name: String, description: String, priority: Priority): Checkpoint {
        if (name.isBlank()) {
            throw InputEmptyException("Checkpoint name must not be blank")
        }
        if (description.isBlank()) {
            throw InputEmptyException("Checkpoint description must not be blank")
        }
        val trimmedName = name.trim()
        val trimmedDescription = description.trim()
        description.trim()
        val checkpoint = Checkpoint(name = trimmedName, description = trimmedDescription, priority = priority)
        return checkpoint
    }

    fun addCheckpoint(name: String, description: String, priority: Priority): PatrolState {
        val checkpoint = createCheckpoint(name, description, priority)
        checkpoints.addLast(checkpoint)
        return state()
    }

    fun addAfterCurrent(name: String, description: String, priority: Priority): PatrolState {
        val checkpoint = createCheckpoint(name, description, priority)
        checkpoints.addAfterCurrent(checkpoint)
        return state()
    }

    fun moveNext(): PatrolState {
        if (checkpoints.isEmpty()) {
            throw PatrolEmptyException("Cannot move to next checkpoint because patrol route is empty")
        }
        checkpoints.next()
        return state()
    }

    fun movePrevious(): PatrolState {
        if (checkpoints.isEmpty()) {
            throw PatrolEmptyException("Cannot move to previous checkpoint because patrol route is empty")
        }
        checkpoints.previous()
        return state()
    }

    fun removeCurrentCheckpoint(): PatrolState {
        if (checkpoints.isEmpty()) {
            throw PatrolEmptyException("Cannot remove current checkpoint because patrol route is empty")
        }
        checkpoints.removeCurrent()
        return state()
    }

    fun allCheckpoints(): List<Checkpoint> {
        return checkpoints.allCheckpoints()
    }

    fun isEmpty(): Boolean {
        return checkpoints.isEmpty()
    }
}
