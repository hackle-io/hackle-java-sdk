package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.TargetRule
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class ExperimentTargetRuleDeterminerTest {

    @MockK
    private lateinit var targetMatcher: TargetMatcher

    @MockK
    private lateinit var bucketer: Bucketer

    @InjectMockKs
    private lateinit var sut: ExperimentTargetRuleDeterminer

    @Test
    fun `첫번째로 매치된 룰을 리턴한다`() {
        // given
        val matchedTargetRule = targetRule(true)
        val experiment = mockk<Experiment> {
            every { targetRules } returns listOf(
                targetRule(false),
                targetRule(false),
                targetRule(false),
                matchedTargetRule,
                targetRule(false),
                targetRule(false),
            )
        }

        // when
        val actual = sut.determineTargetRuleOrNull(mockk(), experiment, mockk())

        // then
        expectThat(actual) isSameInstanceAs matchedTargetRule
        verify(exactly = 4) {
            targetMatcher.matches(any(), any(), any())
        }
    }

    @Test
    fun `매치된 룰이 없으면 null을 리턴한다`() {
        // given
        val experiment = mockk<Experiment> {
            every { targetRules } returns listOf(
                targetRule(false),
                targetRule(false),
                targetRule(false),
                targetRule(false),
                targetRule(false),
            )
        }

        // when
        val actual = sut.determineTargetRuleOrNull(mockk(), experiment, mockk())

        // then
        expectThat(actual).isNull()
        verify(exactly = 5) {
            targetMatcher.matches(any(), any(), any())
        }
    }

    private fun targetRule(isMatch: Boolean): TargetRule {
        val target = mockk<Target>()
        every { targetMatcher.matches(target, any(), any()) } returns isMatch
        return TargetRule(target, mockk())
    }
}