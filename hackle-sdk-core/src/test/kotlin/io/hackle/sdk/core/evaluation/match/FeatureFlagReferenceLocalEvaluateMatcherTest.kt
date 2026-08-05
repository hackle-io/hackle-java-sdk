package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentReferenceLocalEvaluator
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target.Key.Type.FEATURE_FLAG
import io.hackle.sdk.core.model.ValueType.BOOLEAN
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
internal class FeatureFlagReferenceLocalEvaluateMatcherTest {

    @MockK
    private lateinit var evaluator: ExperimentReferenceLocalEvaluator

    @MockK
    private lateinit var valueOperatorMatcher: ValueOperatorMatcher

    @InjectMockKs
    private lateinit var sut: FeatureFlagReferenceLocalEvaluateMatcher

    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        context = Evaluators.context()
    }

    @Test
    fun `match key 가 Long 이 아닌경우`() {
        val featureFlag = Experiments.config(key = 42, type = Experiment.Type.FEATURE_FLAG)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(featureFlags = listOf(featureFlag)),
            experiment = featureFlag
        )
        val condition = Targets.condition(
            key = Targets.key(FEATURE_FLAG, "string"),
            match = Targets.match(valueType = BOOLEAN, values = listOf(true))
        )

        val exception = assertThrows<IllegalArgumentException> {
            sut.matches(request, context, condition)
        }

        expectThat(exception.message) isEqualTo "Invalid key [FEATURE_FLAG, string]"
    }

    @Test
    fun `featureFlag 가 없는 경우 false`() {
        val featureFlag = Experiments.config(key = 42, type = Experiment.Type.FEATURE_FLAG)
        val request = Experiments.localRequest(
            workspace = Workspaces.config(featureFlags = emptyList()),
            experiment = featureFlag
        )
        val condition = Targets.condition(
            key = Targets.key(FEATURE_FLAG, "42"),
            match = Targets.match(valueType = BOOLEAN, values = listOf(true))
        )


        val actual = sut.matches(request, context, condition)

        assertFalse(actual)
        verify { evaluator wasNot Called }
    }

    @Test
    fun `Control Variation 이면 off 로 매칭한다`() {
        val variation = Experiments.variation(key = "A")
        val featureFlag = Experiments.config(
            key = 42,
            type = Experiment.Type.FEATURE_FLAG,
            variations = listOf(variation)
        )
        val request = Experiments.localRequest(
            workspace = Workspaces.config(featureFlags = listOf(featureFlag)),
            experiment = featureFlag
        )

        val evaluation = Experiments.evaluation(
            entity = featureFlag,
            result = Experiments.result(DecisionReason.DEFAULT_RULE, variation)
        )
        every { evaluator.evaluate(any(), any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val condition = Targets.condition(
            key = Targets.key(FEATURE_FLAG, "42"),
            match = Targets.match(valueType = BOOLEAN, values = listOf(true))
        )
        val actual = sut.matches(request, context, condition)

        assertTrue(actual)
        verify { valueOperatorMatcher.matches(false, condition.match) }
    }

    @Test
    fun `Control Variation 이 아니면 on 으로 매칭한다`() {
        val variation = Experiments.variation(key = "B")
        val featureFlag = Experiments.config(
            key = 42,
            type = Experiment.Type.FEATURE_FLAG,
            variations = listOf(variation)
        )
        val request = Experiments.localRequest(
            workspace = Workspaces.config(featureFlags = listOf(featureFlag)),
            experiment = featureFlag
        )

        val evaluation = Experiments.evaluation(
            entity = featureFlag,
            result = Experiments.result(DecisionReason.TARGET_RULE_MATCH, variation)
        )
        every { evaluator.evaluate(any(), any(), any()) } returns evaluation
        every { valueOperatorMatcher.matches(any(), any()) } returns true

        val condition = Targets.condition(
            key = Targets.key(FEATURE_FLAG, "42"),
            match = Targets.match(valueType = BOOLEAN, values = listOf(true))
        )
        val actual = sut.matches(request, context, condition)

        assertTrue(actual)
        verify { valueOperatorMatcher.matches(true, condition.match) }
    }
}
