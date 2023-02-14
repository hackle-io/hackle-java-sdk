package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

internal class IdentifiersBuilderTest {

    @Test
    fun `identifierType은 128자를 넘을 수 없다`() {
        val identifierType = "a".repeat(129)

        val identifiers = IdentifiersBuilder()
            .add(identifierType, "test")
            .build()

        expectThat(identifiers.isEmpty()).isTrue()
    }

    @Test
    fun `identifier value 는 512자를 넘을 수 없다`() {
        val identifiers = IdentifiersBuilder()
            .add("a", "a".repeat(513))
            .build()

        expectThat(identifiers.isEmpty()).isTrue()
    }

    @Test
    fun `identifier value 는 null일 수 없다`() {
        val identifiers = IdentifiersBuilder()
            .add("a", null)
            .build()

        expectThat(identifiers.isEmpty()).isTrue()
    }

    @Test
    fun `identifier value 는 blank 일 수 없다`() {
        val identifiers = IdentifiersBuilder()
            .add("a", " ")
            .build()

        expectThat(identifiers.isEmpty()).isTrue()
    }

    @Test
    fun `identifier value 는 empty 일 수 없다`() {
        val identifiers = IdentifiersBuilder()
            .add("a", "")
            .build()

        expectThat(identifiers.isEmpty()).isTrue()
    }

    @Test
    fun `overwrite`() {
        expectThat(
            IdentifiersBuilder()
                .add("key", "value")
                .add("key", "value2")
                .build()
        ) {
            get { this["key"] } isEqualTo "value2"
        }

        expectThat(
            IdentifiersBuilder()
                .add("key", "value")
                .add("key", "value2", overwrite = false)
                .build()
        ) {
            get { this["key"] } isEqualTo "value"
        }

        expectThat(
            IdentifiersBuilder()
                .add("key", "value2", overwrite = false)
                .build()
        ) {
            get { this["key"] } isEqualTo "value2"
        }
    }

    @Test
    fun `build`() {
        // given

        val identifiers = IdentifiersBuilder()
            .add("a".repeat(128), "a".repeat(512))
            .add(mapOf("a" to "a"))
            .build()


        expectThat(identifiers) {
            hasSize(2)
            isEqualTo(
                mapOf(
                    "a".repeat(128) to "a".repeat(512),
                    "a" to "a"
                )
            )
        }
    }
}