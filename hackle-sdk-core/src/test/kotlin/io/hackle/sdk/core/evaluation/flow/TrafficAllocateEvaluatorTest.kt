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
internal class TrafficAllocateEvaluatorTest {

    @MockK
    private lateinit var actionResolver: ActionResolver

    @InjectMockKs
    private lateinit var sut: TrafficAllocateEvaluator

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
    fun `AB_TEST 타입이 아니면 예외 발생`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.FEATURE_FLAG
        }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(mockk(), experiment, mockk(), "E", mockk())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment type must be AB_TEST")
    }

    @Test
    fun `기본룰에 해당하는 Variation이 없으면 기본그룹으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.AB_TEST
            every { getVariationOrNull(any<String>()) } returns Variation(42, "G", false)
        }

        every { actionResolver.resolveOrNull(any(), any(), any(), any()) } returns null

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("123"), "G", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "G", DecisionReason.TRAFFIC_NOT_ALLOCATED)
    }

    @Test
    fun `할당된 Variation이 드랍되었으면 기본그룹으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.AB_TEST
            every { getVariationOrNull(any<String>()) } returns Variation(42, "G", false)
        }

        val variation = Variation(320, "B", true)

        every { actionResolver.resolveOrNull(any(), any(), any(), any()) } returns variation

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("123"), "G", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "G", DecisionReason.VARIATION_DROPPED)
    }
    
    @Test
    fun `할당된 Variation으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Running>(relaxed = true) {
            every { type } returns Experiment.Type.AB_TEST
        }

        val variation = Variation(320, "B", false)

        every { actionResolver.resolveOrNull(any(), any(), any(), any()) } returns variation

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("123"), "G", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(320, "B", DecisionReason.TRAFFIC_ALLOCATED)
    }
}