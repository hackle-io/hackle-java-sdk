package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.model.ValueType.BOOLEAN
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.Targets
import io.mockk.Called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ExperimentConditionMatcherTest {

    @MockK
    private lateinit var abTestMatcher: AbTestReferenceLocalEvaluateMatcher

    @MockK
    private lateinit var featureFlagMatcher: FeatureFlagReferenceLocalEvaluateMatcher

    @InjectMockKs
    private lateinit var sut: ExperimentConditionMatcher

    @Test
    fun `LocalEvaluateRequest 가 아니면 false`() {
        val request = Experiments.remoteRequest()
        val condition = Targets.condition(
            key = Targets.key(AB_TEST, "42"),
            match = Targets.match(values = listOf("A"))
        )

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertFalse(actual)
        verify { abTestMatcher wasNot Called }
        verify { featureFlagMatcher wasNot Called }
    }

    @Test
    fun `AB_TEST`() {
        val request = Experiments.localRequest(experiment = Experiments.config(type = Experiment.Type.AB_TEST))
        val condition = Targets.condition(
            key = Targets.key(AB_TEST, "42"),
            match = Targets.match(values = listOf("A"))
        )

        every { abTestMatcher.matches(any(), any(), any()) } returns true

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertTrue(actual)
        verify {
            abTestMatcher.matches(request, any(), condition)
        }
    }

    @Test
    fun `FEATURE_FLAG`() {
        val request = Experiments.localRequest(experiment = Experiments.config(type = Experiment.Type.FEATURE_FLAG))
        val condition = Targets.condition(
            key = Targets.key(FEATURE_FLAG, "42"),
            match = Targets.match(valueType = BOOLEAN, values = listOf(true))
        )

        every { featureFlagMatcher.matches(any(), any(), any()) } returns true

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertTrue(actual)
        verify {
            featureFlagMatcher.matches(request, any(), condition)
        }
    }

    @Test
    fun `Unsupported`() {
        val request = Experiments.localRequest()

        fun check(type: Target.Key.Type) {
            val condition = Targets.condition(
                key = Targets.key(type, "42"),
                match = Targets.match(values = listOf("A"))
            )
            assertThrows<IllegalArgumentException> {
                sut.matches(request, Evaluators.context(), condition)
            }
        }
        check(USER_ID)
        check(USER_PROPERTY)
        check(HACKLE_PROPERTY)
        check(SEGMENT)
        check(COHORT)
    }
}
