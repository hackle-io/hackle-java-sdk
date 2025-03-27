package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.condition
import io.hackle.sdk.core.user.HackleUser
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
    fun `Key 에 해당하는 UserValue 가 없어도 operatorMatcher 결과로 매칭한다`() {
        // given
        every { userValueResolver.resolveOrNull(any(), any()) } returns null
        every { valueOperatorMatcher.matches(any(), any()) } returns false

        val condition = condition {
            USER_PROPERTY("grade")
            IN("gold")
        }
        val user = HackleUser.of("1")
        val request = experimentRequest(user = user)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

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
        val request = experimentRequest()

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
        verify(exactly = 1) {
            valueOperatorMatcher.matches(userValue, condition.match)
        }
    }
}
