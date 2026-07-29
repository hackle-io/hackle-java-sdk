package io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.model.DefaultEntity
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.RemoteConfigs
import io.hackle.sdk.core.support.Workspaces
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class RemoteConfigRemoteEvaluatorTest {

    @MockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: RemoteConfigRemoteEvaluator

    @Test
    fun `supports - RemoteConfigRemoteEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(RemoteConfigs.remoteRequest()))
        assertFalse(sut.supports(RemoteConfigs.localRequest()))
    }

    @Test
    fun `entity 의 value 와 reason 으로 평가한다`() {
        // given
        val parameter = RemoteConfigs.remoteResult(
            value = RemoteConfigs.value(id = 320, rawValue = "remoteValue"),
            reason = DecisionReason.TARGET_RULE_MATCH
        )
        val request = RemoteConfigs.remoteRequest(parameter = parameter)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.entity } isSameInstanceAs parameter
            get { evaluation.result.reason } isEqualTo DecisionReason.TARGET_RULE_MATCH
            get { evaluation.result.value } isSameInstanceAs parameter.value
        }
    }

    @Test
    fun `requiredType 과 value 타입이 일치하지 않으면 TYPE_MISMATCH 로 평가한다`() {
        // given
        val parameter = RemoteConfigs.remoteResult(value = RemoteConfigs.value(rawValue = 42))
        val request = RemoteConfigs.remoteRequest(parameter = parameter, requiredType = ValueType.STRING)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.result.reason } isEqualTo DecisionReason.TYPE_MISMATCH
        }
    }

    @Test
    fun `reference 평가 결과를 response 에 포함한다`() {
        // given
        val referenced = Experiments.remoteResult()
        val parameter = RemoteConfigs.remoteResult(
            references = listOf(DefaultEntity(referenced.serviceType, referenced.id))
        )
        val workspace = Workspaces.evaluation(
            experiments = listOf(referenced),
            remoteConfigParameters = listOf(parameter)
        )
        val request = RemoteConfigs.remoteRequest(workspace = workspace, parameter = parameter)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { references }.hasSize(1)
            get { references[0].entity } isSameInstanceAs referenced
        }
    }

    @Test
    fun `record - eventRecorder 로 기록한다`() {
        // given
        justRun { eventRecorder.record(any()) }
        val response = RemoteConfigs.response()

        // when
        sut.record(RemoteConfigs.remoteRequest(), response)

        // then
        verify(exactly = 1) {
            eventRecorder.record(response)
        }
    }
}
