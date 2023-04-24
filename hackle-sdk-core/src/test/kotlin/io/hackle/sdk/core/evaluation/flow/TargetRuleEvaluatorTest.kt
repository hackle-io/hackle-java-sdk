package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.evaluation.target.ExperimentTargetRuleDeterminer
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Experiment.Status.DRAFT
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.model.experiment
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
internal class TargetRuleEvaluatorTest : FlowEvaluatorTest() {

    @MockK
    private lateinit var targetRuleDeterminer: ExperimentTargetRuleDeterminer

    @MockK
    private lateinit var actionResolver: ActionResolver

    @InjectMockKs
    private lateinit var sut: TargetRuleEvaluator

    @Test
    fun `실행중이 아니면 예외 발생`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = DRAFT)
        val request = experimentRequest(experiment = experiment)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, nextFlow)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment status must be RUNNING")
    }

    @Test
    fun `FEATURE_FLAG 타입이 아니면 예외 발생`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING)
        val request = experimentRequest(experiment = experiment)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, nextFlow)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment type must be FEATURE_FLAG")
    }

    @Test
    fun `identifierType에 해당하는 식별자가 없으면 다음 플로우를 실행한다`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING, identifierType = "customId")
        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }

    @Test
    fun `타겟룰에 해당하지 않으면 다음 플로우를 실행한다`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)
        val request = experimentRequest(experiment = experiment)

        every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns null

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
    }

    @Test
    fun `타겟룰에 매치했지만 Action에 해당하는 Variation이 결정되지 않으면 예외 발생`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)
        val request = experimentRequest(experiment = experiment)
        val action = mockk<Action>()
        val targetRule = TargetRule(mockk(), action)

        every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns targetRule

        every { actionResolver.resolveOrNull(any(), any()) } returns null

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, nextFlow)
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("FeatureFlag must decide the Variation")
    }

    @Test
    fun `일치하는 타겟룰이 있는경우 해당 룰에 해당하는 Variation으로 결정한다`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)
        val request = experimentRequest(experiment = experiment)

        val targetRule = mockk<TargetRule> {
            every { action } returns mockk()
        }

        every { targetRuleDeterminer.determineTargetRuleOrNull(any(), any()) } returns targetRule

        every { actionResolver.resolveOrNull(any(), any()) } returns experiment.variations.first()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.TARGET_RULE_MATCH
        expectThat(actual.variationId) isEqualTo experiment.variations.first().id
    }
}