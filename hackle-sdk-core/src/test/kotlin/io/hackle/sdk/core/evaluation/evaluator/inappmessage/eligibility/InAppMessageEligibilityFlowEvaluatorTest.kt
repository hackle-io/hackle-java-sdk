package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.get
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutEvaluation
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout.InAppMessageLayoutEvaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.create
import io.hackle.sdk.core.evaluation.target.InAppMessageFrequencyCapMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
internal class InAppMessageEligibilityFlowEvaluatorTest {


    private lateinit var nextFlow: InAppMessageEligibilityFlow
    private lateinit var evaluation: InAppMessageEligibilityEvaluation
    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        evaluation = InAppMessages.eligibilityEvaluation()
        nextFlow = EvaluationFlow.create(evaluation)
        context = Evaluators.context()
    }

    @Nested
    inner class PlatformInAppMessageEligibilityFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: PlatformInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when inAppMessage does not support android then evaluated as ineligible`() {
            // given
            val inAppMessage =
                InAppMessages.create(messageContext = InAppMessages.messageContext(platformTypes = emptyList()))
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.UNSUPPORTED_PLATFORM
                get { isEligible }.isFalse()
            }
        }


        @Test
        fun `when inAppMessage supports android then evaluate next flow`() {
            // given
            val inAppMessage =
                InAppMessages.create(messageContext = InAppMessages.messageContext(platformTypes = listOf(InAppMessage.PlatformType.ANDROID)))
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isSameInstanceAs(evaluation)
        }
    }


    @Nested
    inner class OverrideInAppMessageEligibilityFlowEvaluatorTest {


        @MockK
        private lateinit var userOverrideMatcher: InAppMessageUserOverrideMatcher


        @InjectMockKs
        private lateinit var sut: OverrideInAppMessageEligibilityFlowEvaluator


        @Test
        fun `when user us overridden then evaluated as OVERRIDDEN`() {
            // given
            val request = InAppMessages.eligibilityRequest()
            val message = request.inAppMessage.messageContext.messages[0]

            every { userOverrideMatcher.matches(any(), any()) } returns true

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.OVERRIDDEN
                get { isEligible }.isTrue()
            }
        }

        @Test
        fun `when user is not overridden then evaluate next flow`() {
            // given
            val request = InAppMessages.eligibilityRequest()
            every { userOverrideMatcher.matches(any(), any()) } returns false

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class DraftInAppMessageEligibilityFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: DraftInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when draft then evaluated as ineligible`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.DRAFT)
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_DRAFT
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `when not draft then evaluate next flow`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.ACTIVE)
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class PauseInAppMessageEligibilityFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: PauseInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when pause then evaluated as ineligible`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.PAUSE)
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_PAUSED
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `when not pause then evaluate next flow`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.ACTIVE)
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class PeriodInAppMessageEligibilityFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: PeriodInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when timestamp is not in inAppMessage period then evaluate as ineligible`() {
            // given
            val inAppMessage = InAppMessages.create(period = InAppMessage.Period.Custom(42, 100))
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage, timestamp = 100)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_PERIOD
                get { isEligible }.isFalse()
            }
        }

        @Test
        fun `when timestamp is in inAppMessage period then evaluate next flow`() {
            // given
            val inAppMessage = InAppMessages.create(period = InAppMessage.Period.Custom(42, 100))
            val request = InAppMessages.eligibilityRequest(inAppMessage = inAppMessage, timestamp = 99)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class TargetInAppMessageEligibilityFlowEvaluatorTest {

        @MockK
        private lateinit var targetMatcher: InAppMessageTargetMatcher

        @InjectMockKs
        private lateinit var sut: TargetInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when user not in inAppMessage target then evaluated as ineligible`() {
            // given
            every { targetMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { isEligible }.isFalse()
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
            }
        }


        @Test
        fun `when user in inAppMessage target then evaluate next flow`() {
            // given
            every { targetMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class LayoutResolveInAppMessageEligibilityFlowEvaluatorTest {

        @MockK
        private lateinit var layoutEvaluator: InAppMessageLayoutEvaluator

        @InjectMockKs
        private lateinit var sut: LayoutResolveInAppMessageEligibilityFlowEvaluator

        @Test
        fun `resolve layout`() {
            // given
            val layoutEvaluation = InAppMessages.layoutEvaluation()
            every { layoutEvaluator.evaluate(any(), any()) } returns layoutEvaluation

            val request = InAppMessages.eligibilityRequest()


            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
            expectThat(context.get<InAppMessageLayoutEvaluation>()) isSameInstanceAs layoutEvaluation
        }
    }

    @Nested
    inner class FrequencyCapInAppMessageEligibilityFlowEvaluatorTest {

        @MockK
        private lateinit var frequencyCapMatcher: InAppMessageFrequencyCapMatcher

        @InjectMockKs
        private lateinit var sut: FrequencyCapInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when frequency capped then evaluated as ineligible`() {
            // given
            every { frequencyCapMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { isEligible }.isFalse()
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_FREQUENCY_CAPPED
            }
        }

        @Test
        fun `when not frequency capped then evaluate next flow`() {
            // given
            every { frequencyCapMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }


    @Nested
    inner class HiddenInAppMessageEligibilityFlowEvaluatorTest {

        @MockK
        private lateinit var hiddenMatcher: InAppMessageHiddenMatcher

        @InjectMockKs
        private lateinit var sut: HiddenInAppMessageEligibilityFlowEvaluator

        @Test
        fun `when hide then evaluated as ineligible`() {
            // given
            every { hiddenMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { isEligible }.isFalse()
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_HIDDEN
            }
        }

        @Test
        fun `when not hide then evaluate next flow`() {
            // given
            every { hiddenMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class EligibleInAppMessageEligibilityFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: EligibleInAppMessageEligibilityFlowEvaluator

        @Test
        fun `evaluate as eligible `() {
            // given
            val request = InAppMessages.eligibilityRequest()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) {
                get { isEligible }.isTrue()
            }
        }
    }
}
