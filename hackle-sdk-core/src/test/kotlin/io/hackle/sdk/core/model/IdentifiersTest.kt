package io.hackle.sdk.core.model

import io.hackle.sdk.common.User
import io.hackle.sdk.core.user.IdentifierType
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.*

class IdentifiersTest {

    @Test
    fun `from user`() {
        val user = User.builder()
            .id("id")
            .userId("userId")
            .deviceId("deviceId")
            .identifier("customId", "customIdValue")
            .build()

        val identifiers = Identifiers.from(user)

        expectThat(identifiers.asMap()).isEqualTo(
            mapOf(
                "\$id" to "id",
                "\$userId" to "userId",
                "\$deviceId" to "deviceId",
                "customId" to "customIdValue",
            )
        )

        expectThat(identifiers.asList()) {
            hasSize(4)
            contains(Identifier("\$id", "id"))
            contains(Identifier("\$userId", "userId"))
            contains(Identifier("\$deviceId", "deviceId"))
            contains(Identifier("customId", "customIdValue"))
        }
    }

    @Test
    fun `from empty map`() {
        val identifiers = Identifiers.from(emptyMap())
        expectThat(identifiers).isEqualTo(Identifiers.empty())
    }

    @Test
    fun `from map`() {
        val map = mapOf("identifierType" to "identifierValue")
        val identifiers = Identifiers.from(map)
        expectThat(identifiers.asMap()).isEqualTo(map)
    }

    @Test
    fun `get identifier`() {
        val map = mapOf(
            "\$id" to "id",
            "\$userId" to "userId",
            "\$deviceId" to "deviceId",
            "customId" to "customIdValue",
        )
        val identifiers = Identifiers.from(map)

        expectThat(identifiers.contains(Identifier("customId", "customIdValue"))).isTrue()
        expectThat(identifiers.contains(Identifier("identifierType", "identifierValue"))).isFalse()

        expectThat(identifiers["customId"]).isEqualTo("customIdValue")
        expectThat(identifiers["customId2"]).isNull()

        expectThat(identifiers[IdentifierType.ID]).isEqualTo("id")
        expectThat(identifiers[IdentifierType.SESSION]).isNull()
    }
}
