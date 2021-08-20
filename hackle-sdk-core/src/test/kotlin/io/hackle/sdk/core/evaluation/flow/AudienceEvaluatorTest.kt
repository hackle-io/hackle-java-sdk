package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.target.TargetAudienceDeterminer
import io.hackle.sdk.core.model.Experiment
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class AudienceEvaluatorTest {

    @MockK
    private lateinit var targetAudienceDeterminer: TargetAudienceDeterminer


    @InjectMockKs
    private lateinit var sut: AudienceEvaluator


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
    fun `사용자가 실험 참여 대상이면 다음 플로우를 실행한다`() {
        // given
        val experiment = mockk<Experiment.Running> {
            every { type } returns Experiment.Type.AB_TEST
        }

        every { targetAudienceDeterminer.isUserInAudiences(any(), experiment, any()) } returns true

        val evaluation = mockk<Evaluation>()
        val nextFlow = mockk<EvaluationFlow> {
            every { evaluate(any(), any(), any(), any()) } returns evaluation
        }

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("123"), "E", nextFlow)

        // then
        expectThat(actual) isSameInstanceAs evaluation
        verify {
            nextFlow.evaluate(any(), experiment, User.of("123"), "E")
        }
    }

    @Test
    fun `사용자가 실험 참여 대상이 아니면 기본그룹으로 평가한다`() {
        // given
        val experiment = mockk<Experiment.Running> {
            every { type } returns Experiment.Type.AB_TEST
        }

        every { targetAudienceDeterminer.isUserInAudiences(any(), experiment, any()) } returns false

        // when
        val actual = sut.evaluate(mockk(), experiment, User.of("123"), "E", mockk())

        // then
        expectThat(actual) isEqualTo  Evaluation(null, "E", DecisionReason.NOT_IN_AUDIENCE)
    }
}
