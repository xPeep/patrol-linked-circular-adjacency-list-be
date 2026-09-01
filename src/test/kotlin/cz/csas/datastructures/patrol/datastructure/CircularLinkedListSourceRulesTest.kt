package cz.csas.datastructures.patrol.datastructure

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.streams.asSequence
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * The assignment forbids using a ready made collection as the storage of the custom
 * list. Reflection catches the obvious case of a collection field, this test also
 * catches local variables, so nobody can build an ArrayList inside a method and copy
 * data back and forth.
 *
 * Only production code is checked, the test sources may use whatever they want.
 */
@DisplayName("CircularLinkedList - assignment rules")
class CircularLinkedListSourceRulesTest {

    private companion object {
        val FORBIDDEN_SYMBOLS = listOf(
            "ArrayList",
            "LinkedList",
            "ArrayDeque",
            "HashMap",
            "LinkedHashMap",
            "HashSet",
            "LinkedHashSet",
            "TreeMap",
            "TreeSet",
            "Vector",
            "Stack",
            "MutableList",
            "MutableMap",
            "MutableSet",
            "MutableCollection",
            "IntArray",
            "arrayOf",
            "arrayOfNulls",
            "arrayListOf",
            "listOf",
            "mutableListOf",
            "mutableMapOf",
            "mutableSetOf",
            "emptyList",
            "emptyMap",
            "buildList",
            "toMutableList",
            "java.util",
        )
    }

    private val sourceFile: Path by lazy { locateSource("CircularLinkedList.kt") }

    @Test
    fun `the implementation file exists where the assignment expects it`() {
        assertTrue(Files.isRegularFile(sourceFile), "CircularLinkedList.kt not found under src/main/kotlin")
    }

    @Test
    fun `the implementation really is a class implementing CircularList`() {
        val code = strippedCode(sourceFile)

        assertTrue(
            Regex("class\\s+CircularLinkedList\\s*<[^>]*>\\s*:\\s*CircularList").containsMatchIn(code),
            "CircularLinkedList must be a generic class implementing CircularList",
        )
    }

    @Test
    fun `no ready made collection is used inside the custom data structure`() {
        val code = strippedCode(sourceFile)

        val offenders = FORBIDDEN_SYMBOLS.filter { symbol ->
            Regex("(?<![A-Za-z0-9_.])" + Regex.escape(symbol) + "(?![A-Za-z0-9_])").containsMatchIn(code)
        }

        if (offenders.isNotEmpty()) {
            fail(
                "CircularLinkedList.kt must not use a ready made collection as its storage. Found: " +
                    offenders.joinToString(", ")
            )
        }
    }

    @Test
    fun `the node keeps both directions of the chain`() {
        val code = strippedCode(sourceFile)

        assertTrue(
            Regex("\\b(var|val)\\s+next\\w*\\s*:").containsMatchIn(code),
            "The node must declare a next reference",
        )
        assertTrue(
            Regex("\\b(var|val)\\s+(previous|prev)\\w*\\s*:").containsMatchIn(code),
            "The node must declare a previous reference, a singly linked list is not enough",
        )
    }

    private fun strippedCode(path: Path): String {
        val raw = Files.readString(path)
        return raw
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), " ")
            .replace(Regex("//[^\\n]*"), " ")
    }

    private fun locateSource(fileName: String): Path {
        val root = Path.of("src", "main", "kotlin")
        if (!Files.isDirectory(root)) {
            fail("src/main/kotlin does not exist, run the tests from the project root")
        }
        Files.walk(root).use { stream ->
            return stream.asSequence().firstOrNull { it.name == fileName }
                ?: fail("$fileName was not found anywhere under src/main/kotlin")
        }
    }
}
