package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.get
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote.InAppMessageLayoutRemoteEvaluator
import io.hackle.sdk.core.support.InAppMessages
import io.mockk.every
import io.mockk.mockk
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityRemoteFlowEvaluatorTest {

    private lateinit var nextFlow: InAppMessageEligibilityRemoteEvaluationFlow
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
    inner class OverrideInAppMessageEligibilityRemoteFlowEvaluatorTest {

        private val sut = OverrideInAppMessageEligibilityRemoteFlowEvaluator()

        @Test
        fun `entity 의 reason 이 OVERRIDDEN 이면 eligible 로 평가한다`() {
            // given
            val inAppMessage = InAppMessages.eligibilityRemoteResult(reason = DecisionReason.OVERRIDDEN)
            val request = InAppMessages.eligibilityRemoteRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.OVERRIDDEN
                get { isEligible }.isTrue()
            }
        }

        @Test
        fun `entity 의 reason 이 OVERRIDDEN 이 아니면 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.eligibilityRemoteResult(reason = DecisionReason.IN_APP_MESSAGE_TARGET)
            val request = InAppMessages.eligibilityRemoteRequest(inAppMessage = inAppMessage)

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
    inner class IneligibleInAppMessageEligibilityRemoteFlowEvaluatorTest {

        private val sut = IneligibleInAppMessageEligibilityRemoteFlowEvaluator()

        @Test
        fun `entity 가 ineligible 이면 entity 의 reason 으로 ineligible 로 평가한다`() {
            // given
            val inAppMessage = InAppMessages.eligibilityRemoteResult(
                isEligible = false,
                reason = DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
            )
            val request = InAppMessages.eligibilityRemoteRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `entity 가 eligible 이면 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.eligibilityRemoteResult(isEligible = true)
            val request = InAppMessages.eligibilityRemoteRequest(inAppMessage = inAppMessage)

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
    inner class LayoutResolveInAppMessageEligibilityRemoteFlowEvaluatorTest {

        @MockK
        private lateinit var layoutEvaluator: InAppMessageLayoutRemoteEvaluator

        @InjectMockKs
        private lateinit var sut: LayoutResolveInAppMessageEligibilityRemoteFlowEvaluator

        @Test
        fun `entity 의 layout 을 평가해 context 에 추가하고 다음 플로우를 실행한다`() {
            // given
            val layoutResponse = InAppMessages.layoutResponse()
            every { layoutEvaluator.evaluate(any(), any()) } returns layoutResponse

            val request = InAppMessages.eligibilityRemoteRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs result
            verify(exactly = 1) {
                nextFlow.evaluate(any(), any())
            }
            expectThat(context.get<InAppMessageLayoutEvaluateResponse>()) isSameInstanceAs layoutResponse
        }
    }
}
