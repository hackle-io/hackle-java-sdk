package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.target.ExperimentTargetDeterminer
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.model.experiment
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class ExperimentTargetEvaluatorTest : FlowEvaluatorTest() {

    @MockK
    private lateinit var experimentTargetDeterminer: ExperimentTargetDeterminer

    @InjectMockKs
    private lateinit var sut: ExperimentTargetEvaluator

    @Test
    fun `AB_TEST 타입이 아니면 예외 발생`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)
        val request = experimentRequest(experiment = experiment)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, nextFlow)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment type must be AB_TEST")
    }

    @Test
    fun `사용자가 실험 참여 대상이면 다음 플로우를 실행한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING)
        val request = experimentRequest(experiment = experiment)

        every { experimentTargetDeterminer.isUserInExperimentTarget(any(), any()) } returns true

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify {
            nextFlow.evaluate(any(), any())
        }
    }

    @Test
    fun `사용자가 실험 참여 대상이 아니면 기본그룹으로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING)
        val request = experimentRequest(experiment = experiment)
        every { experimentTargetDeterminer.isUserInExperimentTarget(any(), any()) } returns false

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.NOT_IN_EXPERIMENT_TARGET
        expectThat(actual.variationKey) isEqualTo "A"
    }
}
