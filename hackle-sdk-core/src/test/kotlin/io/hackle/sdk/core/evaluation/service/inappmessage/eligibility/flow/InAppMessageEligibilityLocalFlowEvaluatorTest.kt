package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.get
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local.InAppMessageLayoutLocalEvaluator
import io.hackle.sdk.core.model.InAppMessage
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
import strikt.assertions.isFalse
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.isTrue

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityLocalFlowEvaluatorTest {

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
    inner class OverrideInAppMessageEligibilityLocalFlowEvaluatorTest {

        @MockK
        private lateinit var userOverrideMatcher: InAppMessageUserOverrideMatcher

        @InjectMockKs
        private lateinit var sut: OverrideInAppMessageEligibilityLocalFlowEvaluator

        @Test
        fun `override 된 사용자면 OVERRIDDEN, eligible 로 평가한다`() {
            // given
            every { userOverrideMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.OVERRIDDEN
                get { isEligible }.isTrue()
            }
        }

        @Test
        fun `override 된 사용자가 아니면 다음 플로우를 실행한다`() {
            // given
            every { userOverrideMatcher.matches(any(), any()) } returns false
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
    inner class DraftInAppMessageEligibilityLocalFlowEvaluatorTest {

        private val sut = DraftInAppMessageEligibilityLocalFlowEvaluator()

        @Test
        fun `DRAFT 상태면 ineligible`() {
            // given
            val inAppMessage = InAppMessages.config(status = InAppMessage.Status.DRAFT)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_DRAFT
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `DRAFT 상태가 아니면 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.config(status = InAppMessage.Status.ACTIVE)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

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
    inner class PauseInAppMessageEligibilityLocalFlowEvaluatorTest {

        private val sut = PauseInAppMessageEligibilityLocalFlowEvaluator()

        @Test
        fun `PAUSE 상태면 ineligible`() {
            // given
            val inAppMessage = InAppMessages.config(status = InAppMessage.Status.PAUSE)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_PAUSED
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `PAUSE 상태가 아니면 다음 플로우를 실행한다`() {
            // given
            val inAppMessage = InAppMessages.config(status = InAppMessage.Status.ACTIVE)
            val request = InAppMessages.eligibilityLocalRequest(inAppMessage = inAppMessage)

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
    inner class TargetInAppMessageEligibilityLocalFlowEvaluatorTest {

        @MockK
        private lateinit var targetMatcher: InAppMessageTargetMatcher

        @InjectMockKs
        private lateinit var sut: TargetInAppMessageEligibilityLocalFlowEvaluator

        @Test
        fun `타겟 대상이 아니면 ineligible`() {
            // given
            every { targetMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.eligibilityLocalRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `타겟 대상이면 다음 플로우를 실행한다`() {
            // given
            every { targetMatcher.matches(any(), any()) } returns true
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
    inner class LayoutResolveInAppMessageEligibilityLocalFlowEvaluatorTest {

        @MockK
        private lateinit var layoutEvaluator: InAppMessageLayoutLocalEvaluator

        @InjectMockKs
        private lateinit var sut: LayoutResolveInAppMessageEligibilityLocalFlowEvaluator

        @Test
        fun `layout 을 평가해 context 에 추가하고 다음 플로우를 실행한다`() {
            // given
            val layoutResponse = InAppMessages.layoutResponse()
            every { layoutEvaluator.evaluate(any(), any()) } returns layoutResponse

            val request = InAppMessages.eligibilityLocalRequest()

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
