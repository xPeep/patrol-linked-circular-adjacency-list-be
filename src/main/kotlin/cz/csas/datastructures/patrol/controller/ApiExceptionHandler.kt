package cz.csas.datastructures.patrol.controller

import cz.csas.datastructures.patrol.dto.ApiErrorResponse
import cz.csas.datastructures.patrol.service.InputEmptyException
import cz.csas.datastructures.patrol.service.PatrolEmptyException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(400).body(
            ApiErrorResponse(
                400,
                "BAD_REQUEST",
                message = e.message ?: e.localizedMessage))
    }

    @ExceptionHandler(PatrolEmptyException::class)
    fun handleEmptyException(e: PatrolEmptyException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(409).body(
            ApiErrorResponse(
                status = 409,
                error = "PATROL_EMPTY",
                message = e.message ?: e.localizedMessage
            )
        )
    }
    @ExceptionHandler(InputEmptyException::class)
    fun handleEmptyException(e: InputEmptyException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(400).body(
            ApiErrorResponse(
                status = 400,
                error = "BAD_REQUEST",
                message = e.message ?: e.localizedMessage
            )
        )
    }
}