package io.hackle.sdk.core.evaluation.rule

import io.hackle.sdk.common.User
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
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class ExperimentTargetRuleMatcherTest {

    @MockK
    private lateinit var targetMatcher: TargetMatcher

    @InjectMockKs
    private lateinit var sut: ExperimentTargetRuleMatcher

    @Test
    fun `실험의 타겟룰 중에서 첫번째로 일치하는 타겟룰을 찾는다`() {
        // given
        val tr1 = targetRule(false)
        val tr2 = targetRule(false)
        val tr3 = targetRule(true)
        val tr4 = targetRule(false)

        val experiment = mockk<Experiment.Running> {
            every { targetRules } returns listOf(tr1, tr2, tr3, tr4)
        }

        // when
        val actual = sut.matchesOrNull(mockk(), experiment, User.of("test"))

        // then
        expectThat(actual)
            .isNotNull()
            .isSameInstanceAs(tr3)

        verify(exactly = 3) {
            targetMatcher.matches(any(), any(), any())
        }
    }

    @Test
    fun `실험의 타겟룰중 일치하는 타겟이 하나도 없으면 null을 리턴헌다`() {
        // given
        val tr1 = targetRule(false)
        val tr2 = targetRule(false)
        val tr3 = targetRule(false)
        val tr4 = targetRule(false)

        val experiment = mockk<Experiment.Running> {
            every { targetRules } returns listOf(tr1, tr2, tr3, tr4)
        }

        // when
        val actual = sut.matchesOrNull(mockk(), experiment, User.of("test"))

        // then
        expectThat(actual)
            .isNull()

        verify(exactly = 4) {
            targetMatcher.matches(any(), any(), any())
        }
    }

    private fun targetRule(isMatch: Boolean): TargetRule {
        val target = mockk<Target>()
        every { targetMatcher.matches(target, any(), any()) } returns isMatch
        return mockk {
            every { this@mockk.target } returns target
        }
    }
}