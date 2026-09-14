package cz.csas.datastructures.patrol.controller

import cz.csas.datastructures.patrol.dto.CheckpointCreateRequest
import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.service.InputEmptyException
import cz.csas.datastructures.patrol.service.PatrolService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class PatrolController(
    private val patrolService: PatrolService,
) {
    @GetMapping("/api/patrol")
    fun apiPatrol(): PatrolState =
        if (patrolService.isEmpty()) {
            PatrolState(
                current = null,
                checkpoints = emptyList()
            )
        } else {
            patrolService.state()
        }

    @PostMapping("/api/checkpoints")
    fun addCheckpoint(
        @RequestBody requestBody: CheckpointCreateRequest
    ): ResponseEntity<Any> =
        if (requestBody.name.isNullOrBlank()) {
            throw InputEmptyException("Checkpoint name must not be blank")
        } else if (requestBody.description.isBlank()) {
            throw InputEmptyException("Checkpoint description must not be blank")
        } else {
            ResponseEntity.status(201).body(
                patrolService.addCheckpoint(
                    requestBody.name,
                    requestBody.description,
                    requestBody.priority
                )
            )
        }

    @PostMapping("/api/patrol/next")
    fun nextPatrol(): PatrolState {
        return patrolService.moveNext()
    }

    @PostMapping("/api/patrol/previous")
    fun previousPatrol(): PatrolState {
       return patrolService.movePrevious()
    }

    @DeleteMapping("/api/checkpoints/current")
    fun removeCurrentPatrol(): PatrolState {
        return patrolService.removeCurrentCheckpoint()
    }
}