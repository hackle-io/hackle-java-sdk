package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.flow.EvaluationFlow
import io.hackle.sdk.core.evaluation.flow.create
import io.hackle.sdk.core.evaluation.target.InAppMessageFrequencyCapMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageHiddenMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageResolver
import io.hackle.sdk.core.evaluation.target.InAppMessageTargetMatcher
import io.hackle.sdk.core.evaluation.target.InAppMessageUserOverrideMatcher
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessages
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class InAppMessageFlowEvaluatorTest {


    private lateinit var nextFlow: InAppMessageFlow
    private lateinit var evaluation: InAppMessageEvaluation
    private lateinit var context: Evaluator.Context

    @BeforeEach
    fun beforeEach() {
        evaluation = InAppMessages.evaluation()
        nextFlow = EvaluationFlow.create(evaluation)
        context = Evaluators.context()
    }

    @Nested
    inner class PlatformInAppMessageFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: PlatformInAppMessageFlowEvaluator

        @Test
        fun `when inAppMessage does not support android then evaluated as null`() {
            // given
            val inAppMessage =
                InAppMessages.create(messageContext = InAppMessages.messageContext(platformTypes = emptyList()))
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.UNSUPPORTED_PLATFORM
            }
        }


        @Test
        fun `when inAppMessage supports android then evaluate next flow`() {
            // given
            val inAppMessage =
                InAppMessages.create(messageContext = InAppMessages.messageContext(platformTypes = listOf(InAppMessage.PlatformType.ANDROID)))
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isSameInstanceAs(evaluation)
        }
    }


    @Nested
    inner class OverrideInAppMessageFlowEvaluatorTest {


        @MockK
        private lateinit var userOverrideMatcher: InAppMessageUserOverrideMatcher

        @MockK
        private lateinit var inAppMessageResolver: InAppMessageResolver

        @InjectMockKs
        private lateinit var sut: OverrideInAppMessageFlowEvaluator


        @Test
        fun `when user us overridden then evaluated as OVERRIDDEN`() {
            // given
            val request = InAppMessages.request()
            val message = request.inAppMessage.messageContext.messages[0]

            every { userOverrideMatcher.matches(any(), any()) } returns true
            every { inAppMessageResolver.resolve(any(), any()) } returns message

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.OVERRIDDEN
                get { this.message } isSameInstanceAs message
            }
        }

        @Test
        fun `when user is not overridden then evalate next flow`() {
            // given
            val request = InAppMessages.request()
            every { userOverrideMatcher.matches(any(), any()) } returns false

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class DraftInAppMessageFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: DraftInAppMessageFlowEvaluator

        @Test
        fun `when draft then evaluated as null`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.DRAFT)
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_DRAFT
                get { message }.isNull()
            }
        }

        @Test
        fun `when not draft then evaluate next flow`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.ACTIVE)
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class PauseInAppMessageFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: PauseInAppMessageFlowEvaluator

        @Test
        fun `when pause then evaluated as null`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.PAUSE)
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_PAUSED
                get { message }.isNull()
            }
        }

        @Test
        fun `when not pause then evaluate next flow`() {
            // given
            val inAppMessage = InAppMessages.create(status = InAppMessage.Status.ACTIVE)
            val request = InAppMessages.request(inAppMessage = inAppMessage)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class PeriodInAppMessageFlowEvaluatorTest {

        @InjectMockKs
        private lateinit var sut: PeriodInAppMessageFlowEvaluator

        @Test
        fun `when timestamp is not in inAppMessage period then evaluate as null`() {
            // given
            val inAppMessage = InAppMessages.create(period = InAppMessage.Period.Custom(42, 100))
            val request = InAppMessages.request(inAppMessage = inAppMessage, timestamp = 100)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { reason } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_PERIOD
                get { message }.isNull()
            }
        }

        @Test
        fun `when timestamp is in inAppMessage period then evaluate next flow`() {
            // given
            val inAppMessage = InAppMessages.create(period = InAppMessage.Period.Custom(42, 100))
            val request = InAppMessages.request(inAppMessage = inAppMessage, timestamp = 99)

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class HiddenInAppMessageFlowEvaluatorTest {

        @MockK
        private lateinit var hiddenMatcher: InAppMessageHiddenMatcher

        @InjectMockKs
        private lateinit var sut: HiddenInAppMessageFlowEvaluator

        @Test
        fun `when hide then evaluated as null`() {
            // given
            every { hiddenMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { message }.isNull()
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_HIDDEN
            }
        }

        @Test
        fun `when not hide then evaluate next flow`() {
            // given
            every { hiddenMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class TargetInAppMessageFlowEvaluatorTest {

        @MockK
        private lateinit var targetMatcher: InAppMessageTargetMatcher

        @MockK
        private lateinit var inAppMessageResolver: InAppMessageResolver

        @InjectMockKs
        private lateinit var sut: TargetInAppMessageFlowEvaluator


        @Test
        fun `when user in inAppMessage target then evaluated to target message`() {
            // given
            every { targetMatcher.matches(any(), any()) } returns true
            every { inAppMessageResolver.resolve(any(), any()) } returns mockk()
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) {
                get { this?.message }.isNotNull()
                get { this?.reason ?: DecisionReason.INVALID_INPUT } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
            }
        }

        @Test
        fun `when user not in inAppMessage target then evaluated as null`() {
            // given
            every { targetMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).and {
                get { this?.message }.isNull()
                get { this?.reason ?: DecisionReason.INVALID_INPUT } isEqualTo DecisionReason.NOT_IN_IN_APP_MESSAGE_TARGET
            }
        }
    }

    @Nested
    inner class FrequencyCapInAppMessageFlowEvaluatorTest {

        @MockK
        private lateinit var frequencyCapMatcher: InAppMessageFrequencyCapMatcher

        @InjectMockKs
        private lateinit var sut: FrequencyCapInAppMessageFlowEvaluator

        @Test
        fun `when frequency capped then evaluated as null`() {
            // given
            every { frequencyCapMatcher.matches(any(), any()) } returns true
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { message }.isNull()
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_FREQUENCY_CAPPED
            }
        }

        @Test
        fun `when not frequency capped then evaluate next flow`() {
            // given
            every { frequencyCapMatcher.matches(any(), any()) } returns false
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class ExperimentInAppMessageFlowEvaluatorTest {

        @MockK
        private lateinit var inAppMessageResolver: InAppMessageResolver

        @InjectMockKs
        private lateinit var sut: ExperimentInAppMessageFlowEvaluator

        @Test
        fun `when control group then evaluated as experiment control group`() {
            // given
            val message = mockk<InAppMessage.Message> {
                every { layout } returns mockk {
                    every { displayType } returns InAppMessage.DisplayType.NONE
                }
            }
            every { inAppMessageResolver.resolve(any(), any()) } returns message
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual?.reason) isEqualTo DecisionReason.EXPERIMENT_CONTROL_GROUP
        }

        @Test
        fun `when not control group then evaluate next flow`() {
            // given
            val message = mockk<InAppMessage.Message> {
                every { layout } returns mockk {
                    every { displayType } returns InAppMessage.DisplayType.MODAL
                }
            }
            every { inAppMessageResolver.resolve(any(), any()) } returns message
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual) isSameInstanceAs evaluation
        }
    }

    @Nested
    inner class MessageResolutionInAppMessageFlowEvaluatorTest {

        @MockK
        private lateinit var inAppMessageResolver: InAppMessageResolver

        @InjectMockKs
        private lateinit var sut: MessageResolutionInAppMessageFlowEvaluator

        @Test
        fun `should resolve message and return evaluation`() {
            // given
            val message = mockk<InAppMessage.Message>()
            every { inAppMessageResolver.resolve(any(), any()) } returns message
            val request = InAppMessages.request()

            // when
            val actual = sut.evaluate(request, context, nextFlow)

            // then
            expectThat(actual).isNotNull().and {
                get { this.message } isSameInstanceAs message
                get { reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
            }
        }
    }
}
