package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.target.OverrideResolver
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.experiment
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class OverrideEvaluatorTest : FlowEvaluatorTest() {

    @MockK
    private lateinit var overrideResolver: OverrideResolver

    @InjectMockKs
    private lateinit var sut: OverrideEvaluator


    @Test
    fun `AbTest 인 경우 override된 사용자인 경우 overriddenVariation, OVERRIDDEN 으로 평가한다`() {
        // given
        val experiment = experiment(type = Experiment.Type.AB_TEST)
        val variation = experiment.variations.first()
        every { overrideResolver.resolveOrNull(any(), any()) } returns variation

        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.OVERRIDDEN
        expectThat(actual.variationId) isEqualTo variation.id
    }

    @Test
    fun `FeatureFlag 인 경우override된 사용자인 경우 overriddenVariation, INDIVIDUAL_TARGET_MATCH 으로 평가한다`() {
        // given
        val experiment = experiment(type = Experiment.Type.FEATURE_FLAG)
        val variation = experiment.variations.first()
        every { overrideResolver.resolveOrNull(any(), any()) } returns variation

        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, Evaluators.context(), nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.INDIVIDUAL_TARGET_MATCH
        expectThat(actual.variationId) isEqualTo variation.id
    }

    @Test
    fun `override된 사용자가 아닌경우 다음 Flow로 평가한다`() {
        // given
        every { overrideResolver.resolveOrNull(any(), any()) } returns null


        // when
        val actual = sut.evaluate(experimentRequest(), Evaluators.context(), nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify(exactly = 1) {
            nextFlow.evaluate(any(), any())
        }
    }
}
