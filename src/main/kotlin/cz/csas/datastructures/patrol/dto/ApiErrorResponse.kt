package cz.csas.datastructures.patrol.dto

data class ApiErrorResponse(
    val status: Int,
    val error: String,
    val message: String
)