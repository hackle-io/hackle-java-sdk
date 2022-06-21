package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.user.HackleUser
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@ExtendWith(MockKExtension::class)
internal class UserValueResolverTest {

    @InjectMockKs
    private lateinit var sut: UserValueResolver

    @Test
    fun `USER_ID`() {
        val user = HackleUser.of("test_user_id")
        expectThat(sut.resolveOrNull(user, Target.Key(USER_ID, "\$id")))
            .isNotNull()
            .isEqualTo("test_user_id")

        expectThat(sut.resolveOrNull(user, Target.Key(USER_ID, "customId")))
            .isNull()
    }

    @Test
    fun `USER_PROPERTY`() {
        val user = HackleUser.of(
            User.builder("test_user_id")
                .property("age", 42)
                .build()
        )
        expectThat(sut.resolveOrNull(user, Target.Key(USER_PROPERTY, "age")))
            .isNotNull()
            .isEqualTo(42)

        expectThat(sut.resolveOrNull(user, Target.Key(USER_PROPERTY, "grade")))
            .isNull()
    }

    @Test
    fun `HACKLE_PROPERTY`() {
        val user = HackleUser.of(
            user = User.builder("test_user_id")
                .property("age", 42)
                .build(),
            hackleProperties = mapOf("os" to "test_os")
        )
        expectThat(sut.resolveOrNull(user, Target.Key(HACKLE_PROPERTY, "os")))
            .isNotNull()
            .isEqualTo("test_os")
        expectThat(sut.resolveOrNull(user, Target.Key(HACKLE_PROPERTY, "os_version")))
            .isNull()
    }

    @Test
    fun `SEGMENT`() {
        val exception = assertThrows<IllegalArgumentException> {
            sut.resolveOrNull(HackleUser.of("id"), Target.Key(SEGMENT, "SEGMENT"))
        }
        expectThat(exception.message)
            .isNotNull()
            .isEqualTo("Unsupported target.key.type [SEGMENT]")
    }
}
