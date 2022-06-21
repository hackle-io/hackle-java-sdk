package io.hackle.sdk.internal.user

import io.hackle.sdk.common.User
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

internal class HackleUserResolverTest {

    private val sut = HackleUserResolver()

    @Test
    fun `식별자가 없는경우 null 리턴`() {
        // given
        val user = User.builder().build()

        // when
        val actual = sut.resolveOrNull(user)

        // then
        expectThat(actual).isNull()
    }

    @Test
    fun `resolve`() {
        // given
        val user = User.builder("id")
            .userId("userId")
            .deviceId("deviceId")
            .identifier("customId", "custom")
            .property("age", 30)
            .property("grade", "GOLD")
            .build()

        // when
        val actual = sut.resolveOrNull(user)

        // then
        expectThat(actual)
            .isNotNull()
            .and {
                get { identifiers } isEqualTo mapOf(
                    "\$id" to "id",
                    "\$userId" to "userId",
                    "\$deviceId" to "deviceId",
                    "customId" to "custom",
                )

                get { properties } isEqualTo mapOf(
                    "age" to 30,
                    "grade" to "GOLD"
                )

                get { hackleProperties }.hasSize(0)
            }
    }
}
