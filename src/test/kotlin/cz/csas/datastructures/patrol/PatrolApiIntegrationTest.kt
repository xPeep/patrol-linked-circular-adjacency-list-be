package cz.csas.datastructures.patrol

import cz.csas.datastructures.patrol.dto.PatrolStateResponse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * End to end run over the real embedded server: the frontend talks to the API
 * exactly like this. Every assertion here mirrors a rule from the assignment.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DisplayName("Patrol API end to end")
class PatrolApiIntegrationTest {

    @Autowired
    private lateinit var rest: TestRestTemplate

    private fun jsonHeaders() = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }

    private fun getState(): ResponseEntity<PatrolStateResponse> =
        rest.getForEntity("/api/patrol", PatrolStateResponse::class.java)

    private fun add(name: String, description: String, priority: String): ResponseEntity<PatrolStateResponse> =
        rest.exchange(
            "/api/checkpoints",
            HttpMethod.POST,
            HttpEntity(
                """{"name":"$name","description":"$description","priority":"$priority"}""",
                jsonHeaders(),
            ),
            PatrolStateResponse::class.java,
        )

    private fun addRaw(body: String): ResponseEntity<String> =
        rest.exchange("/api/checkpoints", HttpMethod.POST, HttpEntity(body, jsonHeaders()), String::class.java)

    private fun next(): ResponseEntity<PatrolStateResponse> =
        rest.exchange("/api/patrol/next", HttpMethod.POST, HttpEntity.EMPTY, PatrolStateResponse::class.java)

    private fun previous(): ResponseEntity<PatrolStateResponse> =
        rest.exchange("/api/patrol/previous", HttpMethod.POST, HttpEntity.EMPTY, PatrolStateResponse::class.java)

    private fun removeCurrent(): ResponseEntity<PatrolStateResponse> =
        rest.exchange(
            "/api/checkpoints/current",
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            PatrolStateResponse::class.java,
        )

    /** Brings the shared in memory route back to an empty state. */
    private fun clearRoute() {
        while (getState().body?.checkpoints?.isNotEmpty() == true) {
            removeCurrent()
        }
    }

    @Test
    fun `the server starts with an empty route and answers 200`() {
        clearRoute()

        val response = getState()

        assertEquals(HttpStatus.OK, response.statusCode)
        val body = assertNotNull(response.body)
        assertNull(body.current)
        assertTrue(body.checkpoints.isEmpty())
    }

    @Test
    fun `a full operator session works over http`() {
        clearRoute()

        val created = add("Entrance", "Main gate", "NORMAL")
        assertEquals(HttpStatus.CREATED, created.statusCode)
        assertEquals("Entrance", created.body?.current?.name)
        assertEquals(1, created.body?.checkpoints?.size)

        next()
        add("Shelf A", "Inventory check", "HIGH")
        next()
        add("Charging Station", "Check battery level", "NORMAL")

        val state = assertNotNull(getState().body)
        assertEquals(
            listOf("Entrance", "Shelf A", "Charging Station"),
            state.checkpoints.map { it.name },
        )
        assertEquals("Shelf A", state.current?.name)

        val afterNext = assertNotNull(next().body)
        assertEquals("Charging Station", afterNext.current?.name)

        val afterWrap = assertNotNull(next().body)
        assertEquals("Entrance", afterWrap.current?.name)

        val afterPrevious = assertNotNull(previous().body)
        assertEquals("Charging Station", afterPrevious.current?.name)

        val afterRemove = assertNotNull(removeCurrent().body)
        assertEquals("Entrance", afterRemove.current?.name)
        assertEquals(listOf("Entrance", "Shelf A"), afterRemove.checkpoints.map { it.name })
    }

    @Test
    fun `every mutation answers with the complete new state`() {
        clearRoute()

        val created = assertNotNull(add("Entrance", "Main gate", "LOW").body)
        assertEquals(1, created.checkpoints.size)

        val moved = assertNotNull(next().body)
        assertEquals(1, moved.checkpoints.size)

        val movedBack = assertNotNull(previous().body)
        assertEquals(1, movedBack.checkpoints.size)

        val removed = assertNotNull(removeCurrent().body)
        assertTrue(removed.checkpoints.isEmpty())
        assertNull(removed.current)
    }

    @Test
    fun `the current checkpoint is always one of the listed checkpoints`() {
        clearRoute()
        add("Entrance", "Main gate", "NORMAL")
        next()
        add("Shelf A", "Inventory check", "HIGH")

        val state = assertNotNull(getState().body)
        val current = assertNotNull(state.current)

        assertTrue(state.checkpoints.any { it.id == current.id })
    }

    @Test
    fun `the server generates the ids and ignores the ones sent by the client`() {
        clearRoute()

        val body = assertNotNull(
            addRaw(
                """{"id":"00000000-0000-0000-0000-000000000000","name":"N","description":"D","priority":"LOW"}"""
            ).body
        )

        assertTrue(body.contains("\"id\""))
        assertTrue(!body.contains("00000000-0000-0000-0000-000000000000"))
    }

    @Test
    fun `operations on an empty route answer 409 and never 500`() {
        clearRoute()

        listOf(
            Triple("/api/patrol/next", HttpMethod.POST, "Cannot move to next checkpoint because patrol route is empty"),
            Triple(
                "/api/patrol/previous",
                HttpMethod.POST,
                "Cannot move to previous checkpoint because patrol route is empty",
            ),
            Triple(
                "/api/checkpoints/current",
                HttpMethod.DELETE,
                "Cannot remove current checkpoint because patrol route is empty",
            ),
        ).forEach { (path, method, message) ->
            val response = rest.exchange(path, method, HttpEntity.EMPTY, String::class.java)

            assertEquals(HttpStatus.CONFLICT, response.statusCode, "$method $path")
            val body = assertNotNull(response.body)
            assertTrue(body.contains("\"status\":409"), body)
            assertTrue(body.contains("\"error\":\"PATROL_EMPTY\""), body)
            assertTrue(body.contains(message), body)
        }
    }

    @Test
    fun `invalid input answers 400 and never 500`() {
        clearRoute()

        listOf(
            """{"name":"","description":"D","priority":"LOW"}""",
            """{"name":"N","description":"","priority":"LOW"}""",
            """{"name":"N","description":"D","priority":"URGENT"}""",
            """{"name":"N","description":"D"}""",
            """{}""",
            """not json at all""",
        ).forEach { payload ->
            val response = addRaw(payload)

            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode, payload)
            val body = assertNotNull(response.body)
            assertTrue(body.contains("\"status\":400"), body)
            assertTrue(body.contains("\"error\":\"BAD_REQUEST\""), body)
        }

        assertTrue(assertNotNull(getState().body).checkpoints.isEmpty())
    }

    @Test
    fun `the response never leaks internal node fields`() {
        clearRoute()
        add("Entrance", "Main gate", "NORMAL")

        val raw = assertNotNull(rest.getForEntity("/api/patrol", String::class.java).body)

        assertTrue(!raw.contains("\"next\""), raw)
        assertTrue(!raw.contains("\"previous\""), raw)
        assertTrue(!raw.contains("\"data\""), raw)
        assertTrue(!raw.contains("\"first\""), raw)
        assertTrue(!raw.contains("\"last\""), raw)
    }

    @Test
    fun `an empty route serializes checkpoints as an empty array, never as null`() {
        clearRoute()

        val raw = assertNotNull(rest.getForEntity("/api/patrol", String::class.java).body)

        assertTrue(raw.contains("\"checkpoints\":[]"), raw)
    }

    @Test
    fun `a preflight request from the configured frontend origin is allowed`() {
        val headers = HttpHeaders().apply {
            set("Origin", "http://localhost:5173")
            set("Access-Control-Request-Method", "POST")
        }

        val response = rest.exchange(
            "/api/checkpoints",
            HttpMethod.OPTIONS,
            HttpEntity<Void>(headers),
            String::class.java,
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("http://localhost:5173", response.headers.getFirst("Access-Control-Allow-Origin"))
    }
}
