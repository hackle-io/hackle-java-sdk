package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target.Key.Type.FEATURE_FLAG
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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
internal class FeatureFlagConditionMatcherTest {
    @MockK
    private lateinit var evaluator: Evaluator

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @InjectMockKs
    private lateinit var sut: FeatureFlagConditionMatcher

    lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        context = Evaluators.context()
    }

    @Test
    fun `key 가 Long 이 아닌경우`() {
        val request = experimentRequest(experiment = experiment(type = Experiment.Type.FEATURE_FLAG))
        val condition = condition {
            key(FEATURE_FLAG, "string")
            match(MATCH, IN, STRING, "A")
        }

        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, context, condition)
        }

        expectThat(exception.message) isEqualTo "Invalid key [FEATURE_FLAG, string]"
    }

    @Test
    fun `experiment 가 없는 경우 false`() {
        val request = experimentRequest(experiment = experiment(type = Experiment.Type.FEATURE_FLAG))
        val condition = condition {
            key(FEATURE_FLAG, "42")
            match(MATCH, IN, STRING, "A")
        }

        val actual = sut.matches(request, context, condition)
        Assertions.assertFalse(actual)
    }

    private fun request(experiment: Experiment): ExperimentRequest {
        val workspace = mockk<Workspace> {
            every { getFeatureFlagOrNull(any()) } returns experiment
        }
        return experimentRequest(workspace, experiment = experiment)
    }

    @Test
    fun `신규 평가`() {
        val request = request(experiment = experiment(type = Experiment.Type.FEATURE_FLAG))
        val condition = condition {
            key(FEATURE_FLAG, "42")
            match(MATCH, IN, STRING, "A")
        }

        val evaluation = evaluation(request, DecisionReason.DEFAULT_RULE)
        every { evaluator.evaluate(any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val actual = sut.matches(request, context, condition)
        assertTrue(actual)

        verify { evaluator.evaluate(any(), any()) }
    }

    @Test
    fun `이미 평가된 경우`() {
        val request = request(experiment = experiment(type = Experiment.Type.FEATURE_FLAG))
        val condition = condition {
            key(FEATURE_FLAG, "42")
            match(MATCH, IN, STRING, "A")
        }

        val evaluation = evaluation(request, DecisionReason.DEFAULT_RULE)
        context.add(evaluation)
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val actual = sut.matches(request, context, condition)
        assertTrue(actual)
        verify { evaluator wasNot Called }
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