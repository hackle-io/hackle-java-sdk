package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.Variation.*
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.model.Experiment.Status.DRAFT
import io.hackle.sdk.core.model.Experiment.Status.RUNNING
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.user.HackleUser
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
        val experiment = experiment(type = AB_TEST, status = DRAFT)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(mockk(), experiment, mockk(), "E", mockk())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("experiment status must be RUNNING")
    }

    @Test
    fun `AB_TEST 타입이 아니면 예외 발생`() {
        // given
        val experiment = experiment(type = FEATURE_FLAG, status = RUNNING)

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
        val experiment = experiment(type = AB_TEST, status = RUNNING) {
            variations {
                G(42)
            }
        }

        every { actionResolver.resolveOrNull(any(), any(), experiment, any()) } returns null

        // when
        val actual = sut.evaluate(mockk(), experiment, HackleUser.of("123"), "G", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "G", DecisionReason.TRAFFIC_NOT_ALLOCATED)
    }

    @Test
    fun `할당된 Variation이 드랍되었으면 기본그룹으로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING) {
            variations {
                A(41, false)
                B(42, false)
                C(43, true)
            }
        }

        every { actionResolver.resolveOrNull(any(), any(), any(), any()) } returns experiment.getVariationOrNull("C")

        // when
        val actual = sut.evaluate(mockk(), experiment, HackleUser.of("123"), "B", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "B", DecisionReason.VARIATION_DROPPED)
    }

    @Test
    fun `할당된 Variation으로 평가한다`() {
        // given
        val experiment = experiment(type = AB_TEST, status = RUNNING) {
            variations {
                A(41, false)
                B(42, false)
            }
        }

        every { actionResolver.resolveOrNull(any(), any(), any(), any()) } returns experiment.getVariationOrNull("B")

        // when
        val actual = sut.evaluate(mockk(), experiment, HackleUser.of("123"), "A", mockk())

        // then
        expectThat(actual) isEqualTo Evaluation(42, "B", DecisionReason.TRAFFIC_ALLOCATED)
    }
}