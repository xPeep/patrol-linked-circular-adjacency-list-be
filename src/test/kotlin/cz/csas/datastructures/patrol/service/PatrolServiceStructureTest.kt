package cz.csas.datastructures.patrol.service

import cz.csas.datastructures.patrol.datastructure.CircularList
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.reflect.Modifier
import kotlin.test.fail

/**
 * The assignment says the service has to sit on top of the custom data structure.
 * Behaviour alone cannot tell the difference, so the route field is inspected directly.
 */
@DisplayName("PatrolService - assignment rules")
class PatrolServiceStructureTest {

    private val fields = PatrolService::class.java.declaredFields
        .filterNot { it.isSynthetic }
        .filterNot { Modifier.isStatic(it.modifiers) }

    @Test
    fun `the route is held by the custom circular list`() {
        val route = fields.firstOrNull { CircularList::class.java.isAssignableFrom(it.type) }

        if (route == null) {
            fail(
                "PatrolService must keep the patrol route in a property of type CircularList. " +
                    "Declared fields: " + fields.map { it.name + ": " + it.type.simpleName }
            )
        }
    }

    @Test
    fun `the route is not held by a ready made collection`() {
        val forbidden = listOf(
            java.util.Collection::class.java,
            java.util.Map::class.java,
        )

        fields.forEach { field ->
            if (field.type.isArray) {
                fail("PatrolService must not keep the route in the array field " + field.name)
            }
            forbidden.forEach { type ->
                if (type.isAssignableFrom(field.type)) {
                    fail(
                        "PatrolService must not keep the route in the field " + field.name +
                            " of type " + field.type.name + ". Use your own CircularLinkedList."
                    )
                }
            }
        }
    }
}
