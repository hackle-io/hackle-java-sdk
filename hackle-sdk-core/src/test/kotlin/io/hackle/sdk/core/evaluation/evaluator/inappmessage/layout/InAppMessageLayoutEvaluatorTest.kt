package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessages
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.workspace.Workspaces
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.*

@ExtendWith(MockKExtension::class)
class InAppMessageLayoutEvaluatorTest {

    @MockK
    private lateinit var evaluator: Evaluator

    @RelaxedMockK
    private lateinit var eventRecorder: EvaluationEventRecorder

    private lateinit var sut: InAppMessageLayoutEvaluator

    @BeforeEach
    fun setUp() {
        val experimentEvaluator = InAppMessageExperimentEvaluator(evaluator)
        val selector = InAppMessageLayoutSelector()
        sut = InAppMessageLayoutEvaluator(experimentEvaluator, selector, eventRecorder)
    }

    @Test
    fun `supports`() {
        expectThat(sut.supports(InAppMessages.layoutRequest())).isTrue()
        expectThat(sut.supports(InAppMessages.eligibilityRequest())).isFalse()
    }

    @Test
    fun `experiment not exist`() {
        // given
        val messageContext = InAppMessages.messageContext(experimentContext = InAppMessage.ExperimentContext(42))
        val inAppMessage = InAppMessages.create(messageContext = messageContext)
        val request = InAppMessages.layoutRequest(inAppMessage = inAppMessage)

        // when
        val exception = assertThrows<IllegalArgumentException> { sut.evaluate(request, Evaluators.context()) }

        // then
        expectThat(exception.message).isEqualTo("Experiment[key=42]")
    }

    @Test
    fun `evaluate by variation`() {
        // given
        val message = InAppMessages.message(variationKey = "B")
        val messageContext = InAppMessages.messageContext(
            experimentContext = InAppMessage.ExperimentContext(42),
            messages = listOf(message)
        )
        val inAppMessage = InAppMessages.create(messageContext = messageContext)

        val experiment = experiment(id = 5, key = 42)
        val workspace = Workspaces.create(experiments = listOf(experiment))

        val request = InAppMessages.layoutRequest(inAppMessage = inAppMessage, workspace = workspace)

        val experimentEvaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, emptyList(), experiment, 320, "B", null)
        every { evaluator.evaluate(any(), any()) } returns experimentEvaluation

        val context = Evaluators.context()

        // when
        val actual = sut.evaluate(request, context)

        // then
        expectThat(actual) {
            get { this.reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
            get { this.targetEvaluations } isEqualTo listOf(experimentEvaluation)
            get { this.message } isEqualTo message
            get { this.properties } isEqualTo hashMapOf(
                "experiment_id" to 5L,
                "experiment_key" to 42L,
                "variation_id" to 320L,
                "variation_key" to "B",
                "experiment_decision_reason" to "TRAFFIC_ALLOCATED"
            )
        }
        expectThat(context) {
            get { this[experiment] } isEqualTo experimentEvaluation
            get { this.properties } isEqualTo actual.properties
        }
    }

    @Test
    fun `evaluate`() {
        // given
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.create(
            messageContext = InAppMessages.messageContext(
                defaultLang = "ko",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.layoutRequest(inAppMessage = inAppMessage)

        // when
        val actual = sut.evaluate(request, Evaluators.context())

        // then
        expectThat(actual) {
            get { this.reason } isEqualTo DecisionReason.IN_APP_MESSAGE_TARGET
            get { this.targetEvaluations }.isEmpty()
            get { this.message } isSameInstanceAs message
            get { this.properties } isEqualTo emptyMap()
        }
    }

    @Test
    fun `cannot evaluate`() {
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.create(
            messageContext = InAppMessages.messageContext(
                defaultLang = "en",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.layoutRequest(inAppMessage = inAppMessage)

        assertThrows<IllegalArgumentException> {
            sut.evaluate(request, Evaluators.context())
        }
    }

    @Test
    fun `cannot evaluate when language matches but variation key mismatches`() {
        // given
        val message = InAppMessages.message(variationKey = "A", lang = "en")
        val messageContext = InAppMessages.messageContext(
            experimentContext = InAppMessage.ExperimentContext(42),
            defaultLang = "en",
            messages = listOf(message)
        )
        val inAppMessage = InAppMessages.create(messageContext = messageContext)

        val experiment = experiment(id = 5, key = 42)
        val workspace = Workspaces.create(experiments = listOf(experiment))
        val request = InAppMessages.layoutRequest(inAppMessage = inAppMessage, workspace = workspace)

        val experimentEvaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, emptyList(), experiment, 320, "B", null)
        every { evaluator.evaluate(any(), any()) } returns experimentEvaluation

        // when & then
        assertThrows<IllegalArgumentException> {
            sut.evaluate(request, Evaluators.context())
        }
    }

    @Test
    fun `record`() {
        // given
        val request = InAppMessages.layoutRequest()
        val evaluation = InAppMessages.layoutEvaluation()

        // when
        sut.record(request, evaluation)

        // then
        verify(exactly = 1) {
            eventRecorder.record(request, evaluation)
        }
    }
}
