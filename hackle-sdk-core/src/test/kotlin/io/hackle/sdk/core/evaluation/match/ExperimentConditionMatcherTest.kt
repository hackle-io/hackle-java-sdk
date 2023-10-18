package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.model.Target.Match.Operator.IN
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ExperimentConditionMatcherTest {

    @MockK
    private lateinit var abTestMatcher: AbTestConditionMatcher

    @MockK
    private lateinit var featureFlagMatcher: FeatureFlagConditionMatcher

    @InjectMockKs
    private lateinit var sut: ExperimentConditionMatcher

    @Test
    fun `AB_TEST`() {
        val request = experimentRequest(experiment = experiment(type = Experiment.Type.AB_TEST))
        val condition = condition {
            AB_TEST("42")
            IN("A")
        }

        every { abTestMatcher.matches(any(), any(), any()) } returns true

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertTrue(actual)
        verify {
            abTestMatcher.matches(request, any(), condition)
        }
    }

    @Test
    fun `FEATURE_FLAG`() {
        val request = experimentRequest(experiment = experiment(type = Experiment.Type.FEATURE_FLAG))
        val condition = condition {
            FEATURE_FLAG("42")
            match(Target.Match.Type.MATCH, IN, ValueType.BOOLEAN, true)
        }

        every { featureFlagMatcher.matches(any(), any(), any()) } returns true

        val actual = sut.matches(request, Evaluators.context(), condition)

        assertTrue(actual)
        verify {
            featureFlagMatcher.matches(request, any(), condition)
        }
    }

    @Test
    fun `Unsupport`() {
        val request = experimentRequest()

        fun verify(type: Target.Key.Type) {
            val condition = condition {
                type("42")
                IN("A")
            }
            assertThrows<IllegalArgumentException> {
                sut.matches(request, Evaluators.context(), condition)
            }
        }
        verify(USER_ID)
        verify(USER_PROPERTY)
        verify(HACKLE_PROPERTY)
        verify(SEGMENT)
        verify(COHORT)
    }
}