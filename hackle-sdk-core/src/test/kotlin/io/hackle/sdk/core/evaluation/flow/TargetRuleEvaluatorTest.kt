package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.rule.ExperimentTargetRuleMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.model.Variation
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class TargetRuleEvaluatorTest {

    @MockK
    private lateinit var targetRuleMatcher: ExperimentTargetRuleMatcher

    @MockK
    private lateinit var actionResolver: ActionResolver

    @InjectMockKs
    private lateinit var sut: TargetRuleEvaluator

    @Test
    fun `실행중이 아니면 예외 발생`() {
        // given
        val experiment = mockk<Experiment>(relaxed = true)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(mockk(), experiment, mockk(), "E", mockk())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment must be running")
    }

    @Test
    fun `FEATURE_FLAG 타입이 아니면 예외 발생`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.AB_TEST
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(mockk(), experiment, mockk(), "E", mockk())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment type must be FEATURE_FLAG")
    }

    @Test
    fun `타겟룰에 해당하지 않으면 다음 플로우를 실행한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.FEATURE_FLAG
        }

        every { targetRuleMatcher.matchesOrNull(any(), any(), any()) } returns null

        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("123"), "E", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }

    @Test
    fun `타겟룰에 매치했지만 Action에 해당하는 Variation이 결정되지 않으면 예외 발생`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.FEATURE_FLAG
        }

        val targetRule = mockk<TargetRule> {
            every { action } returns mockk()
        }

        every { targetRuleMatcher.matchesOrNull(any(), any(), any()) } returns targetRule

        every { actionResolver.resolveOrNull(targetRule.action, any(), experiment, any()) } returns null

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(mockk(), experiment, User.of("123"), "E", mockk())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("FeatureFlag must decide the Variation")
    }

    @Test
    fun `일치하는 타겟룰이 있는경우 해당 룰에 해당하는 Variation으로 결정한다`() {
        // given
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.FEATURE_FLAG
        }

        val targetRule = mockk<TargetRule> {
            every { action } returns mockk()
        }

        every { targetRuleMatcher.matchesOrNull(any(), any(), any()) } returns targetRule

        val variation = Variation(534, "E", false)
        every { actionResolver.resolveOrNull(targetRule.action, any(), experiment, any()) } returns variation

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("154"), "D", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(534, "E", DecisionReason.TARGET_RULE)
    }
}