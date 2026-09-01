package cz.csas.datastructures.patrol.controller

import cz.csas.datastructures.patrol.service.PatrolService
import org.springframework.web.bind.annotation.RestController

/**
 * TODO: the exact HTTP contract the delivered frontend talks to.
 *
 * | HTTP   | Endpoint                 | Request body            | Success response    | Status |
 * |--------|--------------------------|-------------------------|---------------------|--------|
 * | GET    | /api/patrol              | -                       | PatrolStateResponse | 200    |
 * | POST   | /api/checkpoints         | CheckpointCreateRequest | PatrolStateResponse | 201    |
 * | POST   | /api/patrol/next         | -                       | PatrolStateResponse | 200    |
 * | POST   | /api/patrol/previous     | -                       | PatrolStateResponse | 200    |
 * | DELETE | /api/checkpoints/current | -                       | PatrolStateResponse | 200    |
 *
 * Every successful mutation answers with the complete new state, never with just the
 * changed checkpoint. Internal nodes and next / previous references are never serialized.
 *
 * You still have to write yourself:
 *  * `dto/CheckpointCreateRequest` including the validation that turns bad input into 400,
 *  * `dto/ApiErrorResponse` and the `@RestControllerAdvice` that produces it,
 *  * the mapping from the domain model to the response DTOs.
 *
 * CORS is already configured for you in `config/WebCorsConfiguration`.
 */
@RestController
class PatrolController(
    private val patrolService: PatrolService,
)
