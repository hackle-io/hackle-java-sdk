package io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentReferenceLocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.match.InAppMessageLayoutSelector
import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.support.InAppMessages
import io.hackle.sdk.core.support.Workspaces
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

@ExtendWith(MockKExtension::class)
internal class InAppMessageLayoutLocalEvaluatorTest {

    @MockK
    private lateinit var experimentEvaluator: ExperimentReferenceLocalEvaluator

    @RelaxedMockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    private lateinit var sut: InAppMessageLayoutLocalEvaluator

    @BeforeEach
    fun setUp() {
        sut = InAppMessageLayoutLocalEvaluator(experimentEvaluator, InAppMessageLayoutSelector(), eventRecorder)
    }

    @Test
    fun `supports - InAppMessageLayoutLocalEvaluateRequest 만 지원한다`() {
        assertTrue(sut.supports(InAppMessages.layoutLocalRequest()))
        assertFalse(sut.supports(InAppMessages.layoutRemoteRequest()))
        assertFalse(sut.supports(InAppMessages.eligibilityLocalRequest()))
    }

    @Test
    fun `experimentContext 에 해당하는 experiment 가 없으면 예외 발생`() {
        // given
        val inAppMessage = InAppMessages.config(
            messageContext = InAppMessages.messageContext(experimentContext = InAppMessages.experimentContext(key = 42))
        )
        val request = InAppMessages.layoutLocalRequest(inAppMessage = inAppMessage)

        // when
        val exception = assertThrows<IllegalArgumentException> { sut.evaluate(request, Evaluators.context()) }

        // then
        expectThat(exception.message) isEqualTo "Experiment[key=42]"
    }

    @Test
    fun `experimentContext 가 있으면 experiment 평가 결과의 variation 으로 message 를 선택한다`() {
        // given
        val message = InAppMessages.message(variationKey = "B", lang = "ko")
        val inAppMessage = InAppMessages.config(
            messageContext = InAppMessages.messageContext(
                experimentContext = InAppMessages.experimentContext(key = 42),
                messages = listOf(message)
            )
        )

        val experiment = Experiments.config(id = 5, key = 42)
        val workspace = Workspaces.config(experiments = listOf(experiment))
        val request = InAppMessages.layoutLocalRequest(inAppMessage = inAppMessage, workspace = workspace)

        val experimentEvaluation = Experiments.evaluation(
            entity = experiment,
            result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, Experiments.variation(key = "B"))
        )
        every { experimentEvaluator.evaluate(any(), any(), any()) } answers {
            secondArg<Evaluator.Context>().add(experimentEvaluation)
            experimentEvaluation
        }

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.result.reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
            get { evaluation.result.message } isSameInstanceAs message
            get { this.experiment } isSameInstanceAs experimentEvaluation
            get { references }.hasSize(1)
            get { references[0] } isSameInstanceAs experimentEvaluation
        }
    }

    @Test
    fun `experimentContext 평가 시 defaultLang 이 아닌 message 는 제외하고 variation 이 일치하는 message 를 선택한다`() {
        // given
        val otherLangMessage = InAppMessages.message(variationKey = "B", lang = "en")
        val targetMessage = InAppMessages.message(variationKey = "B", lang = "ko")
        val inAppMessage = InAppMessages.config(
            messageContext = InAppMessages.messageContext(
                experimentContext = InAppMessages.experimentContext(key = 42),
                defaultLang = "ko",
                messages = listOf(otherLangMessage, targetMessage)
            )
        )

        val experiment = Experiments.config(id = 5, key = 42)
        val workspace = Workspaces.config(experiments = listOf(experiment))
        val request = InAppMessages.layoutLocalRequest(inAppMessage = inAppMessage, workspace = workspace)

        val experimentEvaluation = Experiments.evaluation(
            entity = experiment,
            result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, Experiments.variation(key = "B"))
        )
        every { experimentEvaluator.evaluate(any(), any(), any()) } answers {
            secondArg<Evaluator.Context>().add(experimentEvaluation)
            experimentEvaluation
        }

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then: lang 불일치 message(en) 는 걸러지고, lang·variation 모두 일치하는 message(ko) 가 선택된다
        expectThat(actual) {
            get { evaluation.result.message } isSameInstanceAs targetMessage
        }
    }

    @Test
    fun `experimentContext 가 없으면 defaultLang 으로 message 를 선택한다`() {
        // given
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.config(
            messageContext = InAppMessages.messageContext(
                defaultLang = "ko",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.layoutLocalRequest(inAppMessage = inAppMessage)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { evaluation.result.reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
            get { evaluation.result.message } isSameInstanceAs message
            get { experiment }.isNull()
        }
    }

    @Test
    fun `message 를 선택하지 못하면 예외 발생`() {
        // given
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.config(
            messageContext = InAppMessages.messageContext(
                defaultLang = "en",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.layoutLocalRequest(inAppMessage = inAppMessage)

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, Evaluators.context())
        }

        // then
        expectThat(exception.message)
            .isNotNull()
            .startsWith("InAppMessage must be decided")
    }

    @Test
    fun `lang 이 일치해도 variationKey 가 일치하지 않으면 예외 발생`() {
        // given
        val message = InAppMessages.message(variationKey = "A", lang = "en")
        val inAppMessage = InAppMessages.config(
            messageContext = InAppMessages.messageContext(
                experimentContext = InAppMessages.experimentContext(key = 42),
                defaultLang = "en",
                messages = listOf(message)
            )
        )

        val experiment = Experiments.config(id = 5, key = 42)
        val workspace = Workspaces.config(experiments = listOf(experiment))
        val request = InAppMessages.layoutLocalRequest(inAppMessage = inAppMessage, workspace = workspace)

        val experimentEvaluation = Experiments.evaluation(
            entity = experiment,
            result = Experiments.result(DecisionReason.TRAFFIC_ALLOCATED, Experiments.variation(key = "B"))
        )
        every { experimentEvaluator.evaluate(any(), any(), any()) } returns experimentEvaluation

        // when & then
        assertThrows<IllegalArgumentException> {
            sut.evaluate(request, Evaluators.context())
        }
    }

    @Test
    fun `record - eventRecorder 로 기록한다`() {
        // given
        val response = InAppMessages.layoutResponse()

        // when
        sut.record(InAppMessages.layoutLocalRequest(), response)

        // then
        verify(exactly = 1) {
            eventRecorder.record(response)
        }
    }
}
