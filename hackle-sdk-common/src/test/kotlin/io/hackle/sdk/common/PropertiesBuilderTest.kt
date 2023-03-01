package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class PropertiesBuilderTest {

    @Test
    fun `raw value valid build`() {
        expectThat(PropertiesBuilder().add("key1", 1).build()).isEqualTo(mapOf("key1" to 1))
        expectThat(PropertiesBuilder().add("key1", "1").build()).isEqualTo(mapOf("key1" to "1"))
        expectThat(PropertiesBuilder().add("key1", true).build()).isEqualTo(mapOf("key1" to true))
    }

    @Test
    fun `array value`() {
        // valid number element
        expectThat(build { add("key1", setOf(1, 2, 3)) }).isEqualTo(mapOf("key1" to listOf(1, 2, 3)))
        expectThat(build { add("key1", listOf(1, 2, 3)) }).isEqualTo(mapOf("key1" to listOf(1, 2, 3)))
        expectThat(build { add("key1", arrayOf(1, 2, 3)) }).isEqualTo(mapOf("key1" to listOf(1, 2, 3)))

        // valid string element
        expectThat(build { add("key1", setOf("1", "2", "3")) }).isEqualTo(mapOf("key1" to listOf("1", "2", "3")))
        expectThat(build { add("key1", listOf("1", "2", "3")) }).isEqualTo(mapOf("key1" to listOf("1", "2", "3")))
        expectThat(build { add("key1", arrayOf("1", "2", "3")) }).isEqualTo(mapOf("key1" to listOf("1", "2", "3")))

        // valid mix element
        expectThat(build { add("key1", listOf("1", 2, "3")) }).isEqualTo(mapOf("key1" to listOf("1", 2, "3")))

        // filter null element
        expectThat(build { add("key1", listOf(1, 2, 3, null, 4)) }).isEqualTo(mapOf("key1" to listOf(1, 2, 3, 4)))

        // filter boolean element
        expectThat(build { add("key1", listOf(true, false)) }).isEqualTo(mapOf("key1" to listOf<Any>()))

        // filter invalid string element
        expectThat(build { add("key1", listOf("a".repeat(1025))) }).isEqualTo(mapOf("key1" to listOf<Any>()))
    }

    @Test
    fun `max property size is 128`() {
        val builder = PropertiesBuilder()
        for (i in (1..128)) {
            builder.add(i.toString(), i)
        }

        expectThat(builder.build()).hasSize(128)

        expectThat(builder.add("key", 42).build()) {
            hasSize(128)
            get { this["key"] }.isNull()
        }
    }

    @Test
    fun `max key length is 128`() {
        val builder = PropertiesBuilder()
        builder.add("a".repeat(128), 128)

        expectThat(builder.build()).hasSize(1)

        builder.add("a".repeat(129), 129)
        expectThat(builder.build()).hasSize(1)
    }

    private fun build(builder: PropertiesBuilder.() -> Unit): Map<String, Any> {
        return PropertiesBuilder().apply(builder).build()
    }
}