package cz.csas.datastructures.patrol.controller

import cz.csas.datastructures.patrol.dto.CheckpointCreateRequest
import cz.csas.datastructures.patrol.dto.PatrolStateResponse
import cz.csas.datastructures.patrol.model.Checkpoint
import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.service.PatrolService
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
    ): PatrolState {
        return patrolService.addAfterCurrent(
            requestBody.name,
            requestBody.description,
            requestBody.priority
        )
    }

    @PostMapping("/api/patrol/next")
    fun nextPatrol(): PatrolState {
        patrolService.moveNext()
        return patrolService.state()
    }

    @PostMapping("/api/patrol/previous")
    fun previousPatrol(): PatrolState {
        patrolService.movePrevious()
        return patrolService.state()
    }

    @DeleteMapping("/api/checkpoints/current")
    fun removeCurrentPatrol(): PatrolState {
        patrolService.removeCurrentCheckpoint()
        return if (patrolService.isEmpty()) {
            PatrolState(
                current = null,
                checkpoints = emptyList()
            )
            patrolService.state()
        } else
            patrolService.state()
    }
}