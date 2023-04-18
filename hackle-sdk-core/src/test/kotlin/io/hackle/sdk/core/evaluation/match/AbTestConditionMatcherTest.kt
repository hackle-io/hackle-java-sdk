package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.remoteConfigRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target.Key.Type.AB_TEST
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.hackle.sdk.core.model.Target.Match.Type.MATCH
import io.hackle.sdk.core.model.ValueType.STRING
import io.hackle.sdk.core.model.condition
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.workspace.Workspace
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class AbTestConditionMatcherTest {

    @MockK
    private lateinit var evaluator: Evaluator

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @InjectMockKs
    private lateinit var sut: AbTestConditionMatcher

    lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        context = Evaluators.context()
    }

    @Test
    fun `key 가 Long 이 아닌경우`() {
        val request = experimentRequest(experiment = experiment(type = Experiment.Type.AB_TEST))
        val condition = condition {
            key(AB_TEST, "string")
            match(MATCH, IN, STRING, "A")
        }

        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, context, condition)
        }

        expectThat(exception.message) isEqualTo "Invalid key [AB_TEST, string]"
    }

    @Test
    fun `experiment 가 없는 경우 false`() {
        val request = experimentRequest(experiment = experiment(type = Experiment.Type.AB_TEST))
        val condition = condition {
            key(AB_TEST, "42")
            match(MATCH, IN, STRING, "A")
        }

        val actual = sut.matches(request, context, condition)
        assertFalse(actual)
    }

    private fun request(experiment: Experiment): ExperimentRequest {
        val workspace = mockk<Workspace> {
            every { getExperimentOrNull(any()) } returns experiment
        }
        return experimentRequest(workspace, experiment = experiment)
    }

    @Test
    fun `매칭 대상 분배사유가 아니면 false`() {

        fun check(reason: DecisionReason) {
            val request = request(experiment(type = Experiment.Type.AB_TEST))
            val condition = condition {
                key(AB_TEST, "42")
                match(MATCH, IN, STRING, "A")
            }

            val evaluation = evaluation(request, reason)
            every { evaluator.evaluate(any(), any()) } returns evaluation

            val actual = sut.matches(request, Evaluators.context(), condition)
            assertFalse(actual)
        }

        check(DecisionReason.EXPERIMENT_DRAFT)
        check(DecisionReason.EXPERIMENT_PAUSED)
        check(DecisionReason.NOT_IN_MUTUAL_EXCLUSION_EXPERIMENT)
        check(DecisionReason.VARIATION_DROPPED)
        check(DecisionReason.NOT_IN_EXPERIMENT_TARGET)
        verify { valueOperatorMatcher wasNot Called }
    }

    @Test
    fun `매칭 대상 분배사유면 Variation 확인`() {
        fun check(reason: DecisionReason) {
            val request = request(experiment = experiment(type = Experiment.Type.AB_TEST))
            val condition = condition {
                key(AB_TEST, "42")
                match(MATCH, IN, STRING, "A")
            }

            val evaluation = evaluation(request, reason)
            every { evaluator.evaluate(any(), any()) } returns evaluation
            every { valueOperatorMatcher.matches(any(), any()) } returns true

            val actual = sut.matches(request, Evaluators.context(), condition)
            assertTrue(actual)
        }

        check(DecisionReason.OVERRIDDEN)
        check(DecisionReason.TRAFFIC_ALLOCATED)
        check(DecisionReason.TRAFFIC_ALLOCATED_BY_TARGETING)
        check(DecisionReason.EXPERIMENT_COMPLETED)
    }

    @Test
    fun `이미 평가된 Experiment 는 다시 평가하지 않는다`() {
        // given
        val request = request(experiment = experiment(type = Experiment.Type.AB_TEST))
        val condition = condition {
            key(AB_TEST, "42")
            match(MATCH, IN, STRING, "A")
        }

        val evaluation = evaluation(request, DecisionReason.TRAFFIC_ALLOCATED)
        every { evaluator.evaluate(any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        context.add(evaluation)

        val actual = sut.matches(request, context, condition)

        assertTrue(actual)
        verify { evaluator wasNot Called }
        expectThat(context.targetEvaluations).hasSize(1)
    }

    @Test
    fun `ExperimentRequest + TRAFFIC_ALLOCATED 인경우 분배 사유를 변경한다`() {
        val request = request(experiment = experiment(type = Experiment.Type.AB_TEST))
        val condition = condition {
            key(AB_TEST, "42")
            match(MATCH, IN, STRING, "A")
        }

        val evaluation = evaluation(request, DecisionReason.TRAFFIC_ALLOCATED)
        every { evaluator.evaluate(any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val actual = sut.matches(request, context, condition)

        assertTrue(actual)
        expectThat(context[request.experiment])
            .isNotNull()
            .get { reason } isEqualTo DecisionReason.TRAFFIC_ALLOCATED_BY_TARGETING
    }

    @Test
    fun `ExperimentRequest + TRAFFIC_ALLOCATED 분배 사유가 아니면 evaluation 그대로 사용`() {
        val request = request(experiment = experiment(type = Experiment.Type.AB_TEST))
        val condition = condition {
            key(AB_TEST, "42")
            match(MATCH, IN, STRING, "A")
        }

        val evaluation = evaluation(request, DecisionReason.OVERRIDDEN)
        every { evaluator.evaluate(any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val actual = sut.matches(request, context, condition)

        assertTrue(actual)
        expectThat(context[request.experiment]) isSameInstanceAs evaluation
    }

    @Test
    fun `ExperimentRequest 가 아니면 evaluation 그대로 사용`() {
        val experimentRequest = request(experiment = experiment(type = Experiment.Type.AB_TEST))
        val request = remoteConfigRequest<Any>(workspace = experimentRequest.workspace)
        val condition = condition {
            key(AB_TEST, "42")
            match(MATCH, IN, STRING, "A")
        }

        val evaluation = evaluation(experimentRequest, DecisionReason.OVERRIDDEN)
        every { evaluator.evaluate(any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val actual = sut.matches(request, context, condition)

        assertTrue(actual)
        expectThat(context[experimentRequest.experiment]) isSameInstanceAs evaluation
    }

    private fun evaluation(request: ExperimentRequest, reason: DecisionReason): ExperimentEvaluation {
        return ExperimentEvaluation.of(
            request,
            context,
            request.experiment.variations.first(),
            reason
        )
    }
}