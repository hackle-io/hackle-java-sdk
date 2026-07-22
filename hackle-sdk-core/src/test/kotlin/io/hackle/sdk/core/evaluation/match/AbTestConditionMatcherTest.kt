package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentReferenceLocalEvaluator
import io.hackle.sdk.core.model.Target.Key.Type.AB_TEST
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Targets
import io.hackle.sdk.core.support.Workspaces
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExtendWith(MockKExtension::class)
internal class AbTestConditionMatcherTest {

    @MockK
    private lateinit var evaluator: ExperimentReferenceLocalEvaluator

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @InjectMockKs
    private lateinit var sut: AbTestReferenceLocalEvaluateMatcher

    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        context = Evaluators.context()
    }

    @Test
    fun `match key 가 Long 이 아닌경우`() {
        val experiment = Experiments.config(key = 42)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(experiments = listOf(experiment)),
            experiment = experiment
        )
        val condition = Targets.condition(
            key = Targets.key(AB_TEST, "string"),
            match = Targets.match(values = listOf("A"))
        )

        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, context, condition)
        }

        expectThat(exception.message) isEqualTo "Invalid key [AB_TEST, string]"
    }

    @Test
    fun `experiment 가 없는 경우 false`() {
        val experiment = Experiments.config(key = 42)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(experiments = emptyList()),
            experiment = experiment
        )
        val condition = Targets.condition(
            key = Targets.key(AB_TEST, "42"),
            match = Targets.match(values = listOf("A"))
        )


        val actual = sut.matches(request, context, condition)

        assertFalse(actual)
        verify { evaluator wasNot Called }
    }

    @Test
    fun `매칭 대상 분배사유가 아니면 false`() {

        fun check(reason: DecisionReason) {
            val experiment = Experiments.config(key = 42)
            val request = Experiments.localRequest(
                workspace = Workspaces.config(experiments = listOf(experiment)),
                experiment = experiment
            )
            val condition = Targets.condition(
                key = Targets.key(AB_TEST, "42"),
                match = Targets.match(values = listOf("A"))
            )

            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(reason, experiment.variations.first())
            )
            every { evaluator.evaluate(any(), any(), any()) } returns evaluation

            val actual = sut.matches(request, context, condition)
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
    fun `매칭 대상 분배사유면 Variation 으로 매칭한다`() {

        fun check(reason: DecisionReason) {
            val experiment = Experiments.config(key = 42)
            val request = Experiments.localRequest(
                workspace = Workspaces.config(experiments = listOf(experiment)),
                experiment = experiment
            )
            val variation = experiment.variations.first()

            val evaluation = Experiments.evaluation(
                entity = experiment,
                result = Experiments.result(reason, variation)
            )
            every { evaluator.evaluate(any(), any(), any()) } returns evaluation
            every { valueOperatorMatcher.matches(any(), any()) } returns true

            val condition = Targets.condition(
                key = Targets.key(AB_TEST, "42"),
                match = Targets.match(values = listOf("A"))
            )
            val actual = sut.matches(request, context, condition)

            assertTrue(actual)
            verify { valueOperatorMatcher.matches(variation.key, condition.match) }
        }

        check(DecisionReason.OVERRIDDEN)
        check(DecisionReason.TRAFFIC_ALLOCATED)
        check(DecisionReason.EXPERIMENT_COMPLETED)
    }
}
