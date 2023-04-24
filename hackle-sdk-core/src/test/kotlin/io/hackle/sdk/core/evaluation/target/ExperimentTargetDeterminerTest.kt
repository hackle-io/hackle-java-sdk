package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ExperimentTargetDeterminerTest {

    @MockK
    private lateinit var targetMatcher: TargetMatcher

    @InjectMockKs
    private lateinit var sut: ExperimentTargetDeterminer

    @Test
    fun `참여대상이 비어있으면 항상 true`() {
        // given
        val request = experimentRequest()

        // when
        val actual = sut.isUserInExperimentTarget(request, Evaluators.context())

        // then
        assertTrue(actual)
    }

    @Test
    fun `실험 참여대상중 하나라도 일치하는게 있으면 match true`() {
        // given
        val experiment = mockk<Experiment> {
            every { id } returns 42
            every { targetAudiences } returns listOf(
                audience(false),
                audience(false),
                audience(true),
                audience(false),
            )
        }

        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.isUserInExperimentTarget(request, Evaluators.context())

        // then
        assertTrue(actual)

        verify(exactly = 3) {
            targetMatcher.matches(any(), any(), any())
        }
    }

    @Test
    fun `실험 참여대상중 일치하는게 하나도 없으면 false`() {
        // given
        val experiment = mockk<Experiment> {
            every { id } returns 42
            every { targetAudiences } returns listOf(
                audience(false),
                audience(false),
                audience(false),
                audience(false),
            )
        }

        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.isUserInExperimentTarget(request, Evaluators.context())

        // then
        assertFalse(actual)

        verify(exactly = 4) {
            targetMatcher.matches(any(), any(), any())
        }
    }

    private fun audience(isMatch: Boolean): Target {
        return mockk<Target>().also {
            every { targetMatcher.matches(any(), any(), it) } returns isMatch
        }
    }
}