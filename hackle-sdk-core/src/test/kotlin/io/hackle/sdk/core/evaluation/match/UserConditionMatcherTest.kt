package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.EvaluationPhase
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.HACKLE_PROPERTY
import io.hackle.sdk.core.model.Target.Key.Type.USER_PROPERTY
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Targets
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.mockk.Called
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

        val condition = Targets.condition(
            key = Targets.key(USER_PROPERTY, "grade"),
            match = Targets.match(values = listOf("gold"))
        )
        val user = HackleUser.builder().identifier(IdentifierType.ID, "1").build()
        val request = Experiments.localRequest(user = user)

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

        val condition = Targets.condition(
            key = Targets.key(USER_PROPERTY, "grade"),
            match = Targets.match(values = listOf("gold"))
        )
        val request = Experiments.localRequest()

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
        verify(exactly = 1) {
            valueOperatorMatcher.matches(userValue, condition.match)
        }
    }

    @Test
    fun `RUNTIME - 미등록 HACKLE_PROPERTY 키도 게이트 없이 매칭한다`() {
        // given
        val condition = Targets.condition(
            key = Targets.key(HACKLE_PROPERTY, "hackle_property"),
            match = Targets.match(values = listOf("value"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.RUNTIME)
        every { userValueResolver.resolveOrNull(any(), any()) } returns "value"
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
        verify(exactly = 1) { valueOperatorMatcher.matches("value", condition.match) }
    }

    @Test
    fun `RUNTIME - SYNC 를 지원하지 않는 HACKLE_PROPERTY 키도 매칭한다`() {
        // given
        val condition = Targets.condition(
            key = Targets.key(HACKLE_PROPERTY, "pagePath"),
            match = Targets.match(values = listOf("/home"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.RUNTIME)
        every { userValueResolver.resolveOrNull(any(), any()) } returns "/home"
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
        verify(exactly = 1) { valueOperatorMatcher.matches("/home", condition.match) }
    }

    @Test
    fun `SYNC - HACKLE_PROPERTY 키가 phase 를 지원하지 않으면 매칭하지 않고 false`() {
        // given
        val condition = Targets.condition(
            key = Targets.key(HACKLE_PROPERTY, "pagePath"),
            match = Targets.match(values = listOf("/home"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.SYNC)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertFalse(actual)
        verify { userValueResolver wasNot Called }
        verify { valueOperatorMatcher wasNot Called }
    }

    @Test
    fun `SYNC - 미등록 HACKLE_PROPERTY 키는 매칭하지 않고 false`() {
        // given
        val condition = Targets.condition(
            key = Targets.key(HACKLE_PROPERTY, "hackle_property"),
            match = Targets.match(values = listOf("value"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.SYNC)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertFalse(actual)
        verify { userValueResolver wasNot Called }
        verify { valueOperatorMatcher wasNot Called }
    }

    @Test
    fun `SYNC - NOT_MATCH 조건이어도 게이트가 선행해서 false`() {
        // given
        val condition = Targets.condition(
            key = Targets.key(HACKLE_PROPERTY, "pagePath"),
            match = Targets.match(type = Target.Match.Type.NOT_MATCH, values = listOf("/home"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.SYNC)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertFalse(actual)
        verify { valueOperatorMatcher wasNot Called }
    }

    @Test
    fun `SYNC - HACKLE_PROPERTY 가 아닌 키에는 게이트를 적용하지 않는다`() {
        // given
        every { userValueResolver.resolveOrNull(any(), any()) } returns "gold"
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val condition = Targets.condition(
            key = Targets.key(USER_PROPERTY, "pagePath"),
            match = Targets.match(values = listOf("gold"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.SYNC)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
        verify(exactly = 1) { valueOperatorMatcher.matches("gold", condition.match) }
    }

    @Test
    fun `SYNC - HACKLE_PROPERTY 키가 phase 를 지원하면 매칭한다`() {
        // given
        every { userValueResolver.resolveOrNull(any(), any()) } returns "Android"
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val condition = Targets.condition(
            key = Targets.key(HACKLE_PROPERTY, "platform"),
            match = Targets.match(values = listOf("Android"))
        )
        val request = Experiments.localRequest(phase = EvaluationPhase.SYNC)

        // when
        val actual = sut.matches(request, Evaluators.context(), condition)

        // then
        assertTrue(actual)
    }
}
