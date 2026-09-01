package cz.csas.datastructures.patrol.controller

import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.service.PatrolService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PatrolController(
    private val patrolService: PatrolService,
) {
    @GetMapping("/api/patrol")
    fun apiPatrol(): PatrolState =
        patrolService.state()

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
}