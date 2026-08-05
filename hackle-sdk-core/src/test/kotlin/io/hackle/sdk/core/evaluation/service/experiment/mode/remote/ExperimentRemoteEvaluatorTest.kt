package io.hackle.sdk.core.evaluation.service.experiment.mode.remote

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.model.DefaultEntity
import io.hackle.sdk.core.support.Experiments
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
internal class ExperimentRemoteEvaluatorTest {

    @MockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    @InjectMockKs
    private lateinit var sut: ExperimentRemoteEvaluator

    @Test
    fun `supports - ExperimentRemoteEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(Experiments.remoteRequest()))
        assertFalse(sut.supports(Experiments.localRequest()))
    }

    @Test
    fun `entity 를 평가 결과로 사용한다`() {
        // given
        val experiment = Experiments.remoteResult(reason = DecisionReason.TRAFFIC_ALLOCATED)
        val request = Experiments.remoteRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.entity } isSameInstanceAs experiment
            get { evaluation.result } isSameInstanceAs experiment
            get { evaluation.result.reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
        }
    }

    @Test
    fun `reference 평가 결과를 response 에 포함한다`() {
        // given
        val referenced = Experiments.remoteResult(id = 1)
        val experiment = Experiments.remoteResult(
            id = 2,
            references = listOf(DefaultEntity(referenced.serviceType, referenced.id))
        )
        val workspace = Workspaces.evaluation(experiments = listOf(referenced, experiment))
        val request = Experiments.remoteRequest(workspace = workspace, experiment = experiment)

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
        val response = Experiments.response()

        // when
        sut.record(Experiments.remoteRequest(), response)

        // then
        verify(exactly = 1) {
            eventRecorder.record(response)
        }
    }
}
