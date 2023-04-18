package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.ParameterConfiguration
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class ExperimentEvaluationTest {

    @Test
    fun `create by Variation`() {
        val experiment = experiment(id = 42, key = 50) {
            variations {
                Variation.A(320, configId = 99)
                Variation.B(321, configId = 100)
            }
        }
        val variation = experiment.getVariationOrNull(321)!!

        val config = mockk<ParameterConfiguration>()
        val workspace = mockk<Workspace> {
            every { getParameterConfigurationOrNull(100) } returns config
        }

        val user = HackleUser.builder().build()
        val request = ExperimentRequest.of(workspace, user, experiment, Variation.H)

        val context = Evaluators.context()
        context.add(mockk<Evaluator.Evaluation>())

        val evaluation = ExperimentEvaluation.of(request, context, variation, DecisionReason.TRAFFIC_ALLOCATED)

        expectThat(evaluation) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            get { targetEvaluations }.hasSize(1)
            get { this.experiment } isSameInstanceAs experiment
            get { variationId } isEqualTo 321
            get { variationKey } isEqualTo "B"
            get { this.config } isSameInstanceAs config
        }
    }

    @Test
    fun `create by Variation - config null`() {
        val experiment = experiment(id = 42, key = 50) {
            variations {
                Variation.A(320)
                Variation.B(321)
            }
        }
        val variation = experiment.getVariationOrNull(321)!!

        val workspace = mockk<Workspace>()

        val user = HackleUser.builder().build()
        val request = ExperimentRequest.of(workspace, user, experiment, Variation.H)

        val context = Evaluators.context()
        context.add(mockk<Evaluator.Evaluation>())

        val evaluation = ExperimentEvaluation.of(request, context, variation, DecisionReason.TRAFFIC_ALLOCATED)

        expectThat(evaluation) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED
            get { targetEvaluations }.hasSize(1)
            get { this.experiment } isSameInstanceAs experiment
            get { variationId } isEqualTo 321
            get { variationKey } isEqualTo "B"
            get { config }.isNull()
        }
    }


    @Test
    fun `create by Variation - config not found`() {
        val experiment = experiment(id = 42, key = 50) {
            variations {
                Variation.A(320)
                Variation.B(321, configId = 100)
            }
        }
        val variation = experiment.getVariationOrNull(321)!!

        val workspace = mockk<Workspace> {
            every { getParameterConfigurationOrNull(100) } returns null
        }

        val user = HackleUser.builder().build()
        val request = ExperimentRequest.of(workspace, user, experiment, Variation.H)

        val context = Evaluators.context()
        context.add(mockk<Evaluator.Evaluation>())

        val exception = assertThrows<IllegalArgumentException> {
            ExperimentEvaluation.of(request, context, variation, DecisionReason.TRAFFIC_ALLOCATED)
        }

        expectThat(exception.message) isEqualTo "ParameterConfiguration[100]"
    }


    @Test
    fun `create by default`() {
        val experiment = experiment(id = 42, key = 50) {
            variations {
                Variation.A(320)
                Variation.B(321)
            }
        }
        val workspace = mockk<Workspace>()

        val user = HackleUser.builder().build()
        val request = ExperimentRequest.of(workspace, user, experiment, Variation.A)

        val evaluation =
            ExperimentEvaluation.ofDefault(request, Evaluators.context(), DecisionReason.TRAFFIC_NOT_ALLOCATED)

        expectThat(evaluation) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
            get { targetEvaluations }.hasSize(0)
            get { this.experiment } isSameInstanceAs experiment
            get { variationId } isEqualTo 320
            get { variationKey } isEqualTo "A"
            get { config }.isNull()
        }
    }

    @Test
    fun `create by default - null`() {
        val experiment = experiment(id = 42, key = 50) {
            variations {
                Variation.A(320)
                Variation.B(321)
            }
        }
        val workspace = mockk<Workspace>()

        val user = HackleUser.builder().build()
        val request = ExperimentRequest.of(workspace, user, experiment, Variation.C)

        val evaluation =
            ExperimentEvaluation.ofDefault(request, Evaluators.context(), DecisionReason.TRAFFIC_NOT_ALLOCATED)

        expectThat(evaluation) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_NOT_ALLOCATED
            get { targetEvaluations }.hasSize(0)
            get { this.experiment } isSameInstanceAs experiment
            get { variationId } isEqualTo null
            get { variationKey } isEqualTo "C"
            get { config }.isNull()
        }
    }

    @Test
    fun `with`() {
        val evaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, mockk(), mockk(), 42, "A", mockk())

        val actual = evaluation.with(DecisionReason.TRAFFIC_ALLOCATED_BY_TARGETING)

        expectThat(actual) {
            get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED_BY_TARGETING
            get { targetEvaluations } isSameInstanceAs evaluation.targetEvaluations
            get { experiment } isSameInstanceAs evaluation.experiment
            get { variationId } isSameInstanceAs evaluation.variationId
            get { variationKey } isSameInstanceAs evaluation.variationKey
            get { config } isSameInstanceAs evaluation.config
        }
    }
}
