package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

internal class EventTest {
    @Test
    fun `build`() {
        val event = Event.builder("purchase")
            .value(42.0)
            .property("k1", "v1")
            .property("k2", 2)
            .properties(mapOf("k3" to true))
            .build()

        expectThat(event) {
            get { key } isEqualTo "purchase"
            get { value } isEqualTo 42.0
            get { properties } isEqualTo mapOf(
                "k1" to "v1",
                "k2" to 2,
                "k3" to true
            )
        }
    }

    @Test
    fun `of`() {
        expectThat(Event.of("purchase")) {
            get { key } isEqualTo "purchase"
            get { value }.isNull()
            get { properties } isEqualTo emptyMap()
        }
    }
}