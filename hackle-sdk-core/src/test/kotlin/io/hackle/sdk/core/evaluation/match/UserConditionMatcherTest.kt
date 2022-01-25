package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.HackleUser
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.condition
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class UserConditionMatcherTest {

    @MockK
    private lateinit var userValueResolver: UserValueResolver

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @InjectMockKs
    private lateinit var sut: UserConditionMatcher

    @Test
    fun `Key 에 해당하는 UserValue 가 없으면 match false`() {
        // given
        every { userValueResolver.resolveOrNull(any(), any()) } returns null
        val condition = condition {
            USER_PROPERTY("grade")
            IN("gold")
        }
        val user = HackleUser.of("1")

        // when
        val actual = sut.matches(condition, mockk(), user)

        // then
        assertFalse(actual)
    }

    @Test
    fun `Key 에 해당하는 UserValue 로 매칭한다`() {
        // given
        val userValue = "test_user_value"
        every { userValueResolver.resolveOrNull(any(), any()) } returns userValue
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val condition = condition {
            USER_PROPERTY("grade")
            IN("gold")
        }
        val user = HackleUser.of("1")

        // when
        val actual = sut.matches(condition, mockk(), user)

        // then
        assertTrue(actual)
        verify(exactly = 1) {
            valueOperatorMatcher.matches("test_user_value", condition.match)
        }
    }
}
