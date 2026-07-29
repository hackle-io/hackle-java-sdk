package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageFrequencyCapMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluateRequest
import io.hackle.sdk.core.model.DayOfWeek
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.support.InAppMessages
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityFlowEvaluatorTest {

    private lateinit var nextFlow: InAppMessageEligibilityLocalEvaluationFlow
    private lateinit var result: InAppMessageEligibilityEvaluateResult
    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        result = InAppMessages.eligibilityResult()
        nextFlow = mockk {
            every { evaluate(any(), any()) } returns result
        }
        context = Evaluators.context()
    }

    @Nested
    inner class PlatformInAppMessageEligibilityFlowEvaluatorTest {

        private val sut = PlatformInAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest>()

        @Test
        fun `platformType 이 없으면 예외 발생`() {
            // given
            val request = InAppMessages.eligibilityLocalRequest(platformType = null)

            // when
            val exception = assertThrows<IllegalArgumentException> {
                sut.evaluate(request, context, nextFlow)
            }

            // then
            expectThat(exception.message) isEqualTo "platformType"
        }

        @Test
        fun `inAppMessage 가 platform 을 지원하지 않으면 ineligible`() {
            // given
            val inAppMessage = InAppMessages.config(
                messageContext = InAppMessages.messageContext(platformTypes = emptyList())
            )
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.UNSUPPORTED_PLATFORM
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `inAppMessage 가 platform 을 지원하면 다음 플로우를 실행한다`() {
            // given
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class PeriodInAppMessageEligibilityFlowEvaluatorTest {

        private val sut = PeriodInAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest>()

        @Test
        fun `timestamp 가 period 에 속하지 않으면 ineligible`() {
            // given
            val inAppMessage = InAppMessages.config(period = InAppMessage.Period.Custom(42, 100))
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage, timestamp = 100)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_PERIOD
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `timestamp 가 period 에 속하면 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.config(period = InAppMessage.Period.Custom(42, 100))
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage, timestamp = 99)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class TimetableInAppMessageEligibilityFlowEvaluatorTest {

        private val sut = TimetableInAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest>()

        private val timetable = InAppMessage.Timetable.Custom(
            listOf(
                InAppMessage.Timetable.Slot(
                    dayOfWeek = DayOfWeek.MONDAY,
                    startMillisInclusive = 9 * 60 * 60 * 1000L, // 09:00
                    endMillisExclusive = 18 * 60 * 60 * 1000L   // 18:00
                )
            )
        )

        @Test
        fun `timestamp 가 timetable 에 속하지 않으면 ineligible`() {
            // given
            val inAppMessage = InAppMessages.config(timetable = timetable)
            // 2025-11-03T08:00:00.000Z (Monday 08:00 - timetable 밖)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage, timestamp = 1762156800000L)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TIMETABLE
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `timestamp 가 timetable 에 속하면 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.config(timetable = timetable)
            // 2025-11-03T12:00:00.000Z (Monday 12:00 - timetable 안)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage, timestamp = 1762171200000L)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }

        @Test
        fun `timetable 이 All 이면 항상 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.config(timetable = InAppMessage.Timetable.All)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage, timestamp = 1762171200000L)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class FrequencyCapInAppMessageEligibilityFlowEvaluatorTest {

        @MockK
        private lateinit var frequencyCapMatcher: InAppMessageFrequencyCapMatcher

        @InjectMockKs
        private lateinit var sut: FrequencyCapInAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest>

        @Test
        fun `frequency cap 에 도달했으면 ineligible`() {
            // given
            every { frequencyCapMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_FREQUENCY_CAPPED
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `frequency cap 에 도달하지 않았으면 다음 플로우를 실행한다`() {
            // given
            every { frequencyCapMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class HiddenInAppMessageEligibilityFlowEvaluatorTest {

        @MockK
        private lateinit var hiddenMatcher: InAppMessageHiddenMatcher

        @InjectMockKs
        private lateinit var sut: HiddenInAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest>

        @Test
        fun `숨김 처리된 경우 ineligible`() {
            // given
            every { hiddenMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_HIDDEN
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `숨김 처리되지 않은 경우 다음 플로우를 실행한다`() {
            // given
            every { hiddenMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
        }
    }

    @Nested
    inner class EligibleInAppMessageEligibilityFlowEvaluatorTest {

        private val sut = EligibleInAppMessageEligibilityFlowEvaluator<InAppMessageEligibilityLocalEvaluateRequest>()

        @Test
        fun `eligible 로 평가한다`() {
            // given
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
                get { isEligible }.isTrue()
            }
        }
    }
}
