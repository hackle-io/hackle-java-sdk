package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class RemoteConfigLocalEvaluatorTest {

    @MockK
    private lateinit var targetRuleDeterminer: RemoteConfigParameterTargetRuleDeterminer

    @MockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: RemoteConfigLocalEvaluator

    @Test
    fun `supports - RemoteConfigLocalEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(RemoteConfigs.localRequest()))
        assertFalse(sut.supports(RemoteConfigs.remoteRequest()))
    }

    @Nested
    inner class EvaluateTest {

        @Test
        fun `순환 호출이 발생하면 예외 발생`() {
            // given
            val request = RemoteConfigs.localRequest()
            val context = Evaluators.context()
            context.add(request)

            // when
            val exception = assertThrows<IllegalArgumentException> { sut.evaluate(request, context) }

            // then
            expectThat(exception.message)
                .isNotNull()
                .startsWith("Circular evaluation has occurred")
        }

        @Test
        fun `식별자가 없으면 IDENTIFIER_NOT_FOUND 로 평가한다`() {
            // given
            val parameter = RemoteConfigs.config(identifierType = "customId")
            val request = RemoteConfigs.localRequest(parameter = parameter)

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.entity } isSameInstanceAs parameter
                get { evaluation.result.reason } isEqualTo DecisionReason.IDENTIFIER_NOT_FOUND
                get { evaluation.result.value }.isNull()
            }
            verify { targetRuleDeterminer wasNot Called }
        }

        @Test
        fun `TargetRule 에 해당하면 TargetRule 의 value, TARGET_RULE_MATCH 로 평가한다`() {
            // given
            val targetRule = RemoteConfigs.targetRule(
                value = RemoteConfigs.value(id = 320, rawValue = "targetRuleValue")
            )
            val parameter = RemoteConfigs.config(targetRules = listOf(targetRule))
            val request = RemoteConfigs.localRequest(parameter = parameter)

            every { targetRuleDeterminer.determine(any(), any()) } returns targetRule

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.result.reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
                get { evaluation.result.value } isSameInstanceAs targetRule.value
            }
        }

        @Test
        fun `TargetRule 에 해당하지 않으면 defaultValue, DEFAULT_RULE 로 평가한다`() {
            // given
            val defaultValue = RemoteConfigs.value(id = 43, rawValue = "defaultValue")
            val parameter = RemoteConfigs.config(defaultValue = defaultValue)
            val request = RemoteConfigs.localRequest(parameter = parameter)

            every { targetRuleDeterminer.determine(any(), any()) } returns null

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.result.reason } isEqualTo DecisionReason.DEFAULT_RULE
                get { evaluation.result.value } isSameInstanceAs defaultValue
            }
        }

        @Test
        fun `context 에 추가된 reference 평가를 response 에 포함한다`() {
            // given
            every { targetRuleDeterminer.determine(any(), any()) } returns null

            val referenceEvaluation = Experiments.evaluation()
            val context = Evaluators.context()
            context.add(referenceEvaluation)

            // when
            val actual = sut.evaluate(RemoteConfigs.localRequest(), context)

            // then
            expectThat(actual) {
                get { references }.hasSize(1)
                get { references[0] } isSameInstanceAs referenceEvaluation
            }
        }

        @Test
        fun `requiredType 과 value 타입이 일치하지 않으면 TYPE_MISMATCH 로 평가한다`() {
            // given
            val parameter = RemoteConfigs.config(defaultValue = RemoteConfigs.value(rawValue = 42))
            val request = RemoteConfigs.localRequest(parameter = parameter, requiredType = ValueType.STRING)

            every { targetRuleDeterminer.determine(any(), any()) } returns null

            // when
            val actual = sut.evaluate(request, Evaluators.context())

            // then
            expectThat(actual) {
                get { evaluation.result.reason } isEqualTo DecisionReason.TYPE_MISMATCH
            }
        }
    }

    @Test
    fun `record - eventRecorder 로 기록한다`() {
        // given
        justRun { eventRecorder.record(any()) }
        val response = RemoteConfigs.response()

        // when
        sut.record(RemoteConfigs.localRequest(), response)

        // then
        verify(exactly = 1) {
            eventRecorder.record(response)
        }
    }
}
