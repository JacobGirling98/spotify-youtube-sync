package unit.util

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.example.util.combine
import kotlin.test.Test

class MapTest {

    @Test
    fun `can combine a map with an empty map`() {
        val map = mapOf("a" to 1)
        val emptyMap = emptyMap<String, Int>()

        map.combine(emptyMap) { key, first, second -> first } shouldBe map
    }

    @Test
    fun `can combine an empty map with a map`() {
        val map = mapOf("a" to 1)
        val emptyMap = emptyMap<String, Int>()

        emptyMap.combine(map) { key, first, second -> first } shouldBe map
    }

    @Test
    fun `can combine maps with different keys without calling the callback`() {
        val map = mapOf("a" to 1)
        val otherMap = mapOf("b" to 2)

        map.combine(otherMap) { key, first, second -> first } shouldBe mapOf("a" to 1, "b" to 2)
    }

    @Test
    fun `uses callback result for matching key`() {
        val map = mapOf("a" to 1)
        val otherMap = mapOf("a" to 2)

        map.combine(otherMap) { key, first, second -> first } shouldBe mapOf("a" to 1)
    }

    @Test
    fun `can access the current key if having to merge values`() {
        val map = mapOf("a" to 1)
        val otherMap = mapOf("a" to 2, "b" to 3)

        map.combine(otherMap) { key, first, second ->
            key shouldBe "a"
            key shouldNotBe "b"
            first
        }
    }

    @Test
    fun `can handle nullable values when merging`() {
        val map = mapOf<String, Int?>("a" to 1)
        val otherMap = mapOf<String, Int?>("a" to null, "b" to 3)

        val combined = map.combine(otherMap) { key, first, second ->
            when {
                first == null -> first
                else -> second
            }
        }

        combined shouldBe mapOf("a" to null, "b" to 3)
    }
}