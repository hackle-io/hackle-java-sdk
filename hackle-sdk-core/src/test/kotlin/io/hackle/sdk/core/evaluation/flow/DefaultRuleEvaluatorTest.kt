package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.model.Experiment
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
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class DefaultRuleEvaluatorTest {

    @MockK
    private lateinit var actionResolver: ActionResolver

    @InjectMockKs
    private lateinit var sut: DefaultRuleEvaluator

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
    fun `기본룰에 해당하는 Variation을 결정하지 못하면 예외 발생`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.FEATURE_FLAG
            every { defaultRule } returns mockk()
        }

        every { actionResolver.resolveOrNull(experiment.defaultRule, any(), experiment, any()) } returns null

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
    fun `기본룰에 해당하는 Variation으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.FEATURE_FLAG
            every { defaultRule } returns mockk()
        }

        val variation = Variation(513, "H", false)

        every { actionResolver.resolveOrNull(experiment.defaultRule, any(), experiment, any()) } returns variation

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("15"), "A", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(513, "H", DecisionReason.DEFAULT_RULE)
    }
}