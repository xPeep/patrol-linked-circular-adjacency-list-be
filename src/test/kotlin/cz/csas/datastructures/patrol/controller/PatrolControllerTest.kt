package cz.csas.datastructures.patrol.controller

import cz.csas.datastructures.patrol.model.Checkpoint
import cz.csas.datastructures.patrol.model.PatrolState
import cz.csas.datastructures.patrol.model.Priority
import cz.csas.datastructures.patrol.service.PatrolEmptyException
import cz.csas.datastructures.patrol.service.PatrolService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willThrow
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * The HTTP contract the delivered frontend relies on.
 *
 * The service is mocked here on purpose: this class only proves that the URLs,
 * the HTTP methods, the status codes and the JSON shapes are exactly as documented.
 */
@WebMvcTest(PatrolController::class)
@DisplayName("REST contract")
class PatrolControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var patrolService: PatrolService

    private val entrance = Checkpoint(
        id = UUID.fromString("8bc6631b-2c80-4728-b45f-e7e43df27557"),
        name = "Entrance",
        description = "Main gate",
        priority = Priority.NORMAL,
    )

    private val shelfA = Checkpoint(
        id = UUID.fromString("9f2e4d8a-2140-45b9-a7b0-2dc9d989bd35"),
        name = "Shelf A",
        description = "Inventory check",
        priority = Priority.HIGH,
    )

    private val filledState = PatrolState(current = shelfA, checkpoints = listOf(entrance, shelfA))
    private val emptyState = PatrolState(current = null, checkpoints = emptyList())

    @Nested
    @DisplayName("GET /api/patrol")
    inner class GetPatrol {

        @Test
        fun `returns 200 and the whole state`() {
            given(patrolService.state()).willReturn(filledState)

            mockMvc.perform(get("/api/patrol"))
                .andExpect(status().isOk)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.current.id").value(shelfA.id.toString()))
                .andExpect(jsonPath("$.current.name").value("Shelf A"))
                .andExpect(jsonPath("$.current.description").value("Inventory check"))
                .andExpect(jsonPath("$.current.priority").value("HIGH"))
                .andExpect(jsonPath("$.checkpoints.length()").value(2))
                .andExpect(jsonPath("$.checkpoints[0].id").value(entrance.id.toString()))
                .andExpect(jsonPath("$.checkpoints[0].name").value("Entrance"))
                .andExpect(jsonPath("$.checkpoints[1].name").value("Shelf A"))
        }

        @Test
        fun `returns 200 with a null current and an empty array for an empty route`() {
            given(patrolService.state()).willReturn(emptyState)

            mockMvc.perform(get("/api/patrol"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.current").doesNotExist())
                .andExpect(jsonPath("$.checkpoints").isArray)
                .andExpect(jsonPath("$.checkpoints.length()").value(0))
        }

        @Test
        fun `never exposes internal node references`() {
            given(patrolService.state()).willReturn(filledState)

            mockMvc.perform(get("/api/patrol"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.checkpoints[0].next").doesNotExist())
                .andExpect(jsonPath("$.checkpoints[0].previous").doesNotExist())
                .andExpect(jsonPath("$.checkpoints[0].data").doesNotExist())
                .andExpect(jsonPath("$.current.next").doesNotExist())
                .andExpect(jsonPath("$.current.previous").doesNotExist())
        }

        @Test
        fun `a checkpoint has exactly the four documented fields`() {
            given(patrolService.state()).willReturn(filledState)

            mockMvc.perform(get("/api/patrol"))
                .andExpect(jsonPath("$.checkpoints[0].length()").value(4))
                .andExpect(jsonPath("$.length()").value(2))
        }
    }

    @Nested
    @DisplayName("POST /api/checkpoints")
    inner class AddCheckpoint {

        @Test
        fun `returns 201 and the whole new state`() {
            given(patrolService.addCheckpoint("Charging Station", "Check battery level", Priority.NORMAL))
                .willReturn(filledState)

            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"Charging Station","description":"Check battery level","priority":"NORMAL"}
                        """.trimIndent()
                    )
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.current.name").value("Shelf A"))
                .andExpect(jsonPath("$.checkpoints.length()").value(2))
        }

        @Test
        fun `accepts every documented priority`() {
            given(patrolService.addCheckpoint("N", "D", Priority.LOW)).willReturn(filledState)
            given(patrolService.addCheckpoint("N", "D", Priority.NORMAL)).willReturn(filledState)
            given(patrolService.addCheckpoint("N", "D", Priority.HIGH)).willReturn(filledState)

            listOf("LOW", "NORMAL", "HIGH").forEach { priority ->
                mockMvc.perform(
                    post("/api/checkpoints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"name":"N","description":"D","priority":"$priority"}""")
                )
                    .andExpect(status().isCreated)
            }
        }

        @Test
        fun `rejects a blank name with 400 and the documented error body`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"   ","description":"D","priority":"NORMAL"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Checkpoint name must not be blank"))

            verifyNoInteractions(patrolService)
        }

        @Test
        fun `rejects a missing name with 400`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"description":"D","priority":"NORMAL"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Checkpoint name must not be blank"))
        }

        @Test
        fun `rejects a blank description with 400`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"N","description":"","priority":"NORMAL"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("Checkpoint description must not be blank"))
        }

        @Test
        fun `rejects a missing priority with 400`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"N","description":"D"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("Checkpoint priority must be one of LOW, NORMAL, HIGH"))
        }

        @Test
        fun `rejects an unknown priority with 400 and not with 500`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"N","description":"D","priority":"URGENT"}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Checkpoint priority must be one of LOW, NORMAL, HIGH"))
        }

        @Test
        fun `rejects a lower case priority`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"N","description":"D","priority":"normal"}""")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `rejects a malformed body with 400 and not with 500`() {
            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ this is not json ")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
        }

        @Test
        fun `ignores a client supplied id`() {
            given(patrolService.addCheckpoint("N", "D", Priority.NORMAL)).willReturn(filledState)

            mockMvc.perform(
                post("/api/checkpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """{"id":"11111111-1111-1111-1111-111111111111","name":"N","description":"D","priority":"NORMAL"}"""
                    )
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.checkpoints[0].id").value(entrance.id.toString()))
        }
    }

    @Nested
    @DisplayName("POST /api/patrol/next")
    inner class MoveNext {

        @Test
        fun `returns 200 and the whole new state`() {
            given(patrolService.moveNext()).willReturn(filledState)

            mockMvc.perform(post("/api/patrol/next"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.current.name").value("Shelf A"))
                .andExpect(jsonPath("$.checkpoints.length()").value(2))
        }

        @Test
        fun `returns 409 with the documented error body for an empty route`() {
            willThrow(PatrolEmptyException("Cannot move to next checkpoint because patrol route is empty"))
                .given(patrolService).moveNext()

            mockMvc.perform(post("/api/patrol/next"))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("PATROL_EMPTY"))
                .andExpect(
                    jsonPath("$.message").value("Cannot move to next checkpoint because patrol route is empty")
                )
        }
    }

    @Nested
    @DisplayName("POST /api/patrol/previous")
    inner class MovePrevious {

        @Test
        fun `returns 200 and the whole new state`() {
            given(patrolService.movePrevious()).willReturn(filledState)

            mockMvc.perform(post("/api/patrol/previous"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.current.name").value("Shelf A"))
        }

        @Test
        fun `returns 409 with the documented error body for an empty route`() {
            willThrow(PatrolEmptyException("Cannot move to previous checkpoint because patrol route is empty"))
                .given(patrolService).movePrevious()

            mockMvc.perform(post("/api/patrol/previous"))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("PATROL_EMPTY"))
                .andExpect(
                    jsonPath("$.message").value("Cannot move to previous checkpoint because patrol route is empty")
                )
        }
    }

    @Nested
    @DisplayName("DELETE /api/checkpoints/current")
    inner class RemoveCurrent {

        @Test
        fun `returns 200 and the whole new state`() {
            given(patrolService.removeCurrentCheckpoint()).willReturn(filledState)

            mockMvc.perform(delete("/api/checkpoints/current"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.checkpoints.length()").value(2))
        }

        @Test
        fun `returns 200 with an empty state when the last checkpoint is removed`() {
            given(patrolService.removeCurrentCheckpoint()).willReturn(emptyState)

            mockMvc.perform(delete("/api/checkpoints/current"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.current").doesNotExist())
                .andExpect(jsonPath("$.checkpoints.length()").value(0))
        }

        @Test
        fun `returns 409 with the documented error body for an empty route`() {
            willThrow(PatrolEmptyException("Cannot remove current checkpoint because patrol route is empty"))
                .given(patrolService).removeCurrentCheckpoint()

            mockMvc.perform(delete("/api/checkpoints/current"))
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.error").value("PATROL_EMPTY"))
                .andExpect(
                    jsonPath("$.message").value("Cannot remove current checkpoint because patrol route is empty")
                )
        }
    }

    @Nested
    @DisplayName("routing")
    inner class Routing {

        @Test
        fun `the endpoints live under the api prefix`() {
            given(patrolService.state()).willReturn(emptyState)

            mockMvc.perform(get("/patrol")).andExpect(status().isNotFound)
            mockMvc.perform(get("/api/patrol")).andExpect(status().isOk)
        }

        @Test
        fun `wrong http methods are not silently accepted`() {
            mockMvc.perform(get("/api/patrol/next")).andExpect(status().isMethodNotAllowed)
            mockMvc.perform(get("/api/checkpoints/current")).andExpect(status().isMethodNotAllowed)
        }
    }

    @Nested
    @DisplayName("CORS")
    inner class Cors {

        @Test
        fun `a preflight request from the frontend origin is allowed`() {
            mockMvc.perform(
                options("/api/checkpoints")
                    .header("Origin", "http://localhost:5173")
                    .header("Access-Control-Request-Method", "POST")
            )
                .andExpect(status().isOk)
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
        }

        @Test
        fun `a simple request from the frontend origin carries the CORS header`() {
            given(patrolService.state()).willReturn(emptyState)

            mockMvc.perform(get("/api/patrol").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk)
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
        }
    }
}
