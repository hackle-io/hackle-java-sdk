package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.A
import io.hackle.sdk.common.Variation.B
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.evaluator.experiment.experimentRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
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
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class DefaultRuleEvaluatorTest : FlowEvaluatorTest() {

    @MockK
    private lateinit var actionResolver: ActionResolver

    @InjectMockKs
    private lateinit var sut: DefaultRuleEvaluator

    @Test
    fun `실행중이 아니면 예외 발생`() {
        // given
        val experiment = mockk<Experiment>(relaxed = true)
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
    fun `기본룰에 해당하는 Variation을 결정하지 못하면 예외 발생`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)
        val request = experimentRequest(experiment = experiment)
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
    fun `identifierType에 해당하는 식별자가 없으면 defaultVariation 을 리턴한다`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING, identifierType = "customId") {
            variations {
                A(41, false)
                B(42, false)
            }
        }
        val request = experimentRequest(experiment = experiment)

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.DEFAULT_RULE
        expectThat(actual.variationId) isEqualTo 41
    }

    @Test
    fun `기본룰에 해당하는 Variation으로 평가한다`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)
        val request = experimentRequest(experiment = experiment)

        every { actionResolver.resolveOrNull(any(), any()) } returns experiment.variations.first()

        // when
        val actual = sut.evaluate(request, context, nextFlow)

        // then
        expectThat(actual.reason) isEqualTo DecisionReason.DEFAULT_RULE
        expectThat(actual.variationId) isEqualTo experiment.variations.first().id
    }
}
