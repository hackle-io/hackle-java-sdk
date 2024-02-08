package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.InAppMessages
import io.hackle.sdk.core.model.experiment
import io.hackle.sdk.core.workspace.Workspaces
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
internal class InAppMessageResolverTest {

    @MockK
    private lateinit var evaluator: Evaluator

    @InjectMockKs
    private lateinit var sut: InAppMessageResolver

    @Test
    fun `experiment not exist`() {
        // given
        val messageContext = InAppMessages.messageContext(experimentContext = InAppMessage.ExperimentContext(42))
        val inAppMessage = InAppMessages.create(messageContext = messageContext)
        val request = InAppMessages.request(inAppMessage = inAppMessage)

        // when
        val exception = assertThrows<IllegalArgumentException> { sut.resolve(request, Evaluators.context()) }

        // then
        expectThat(exception.message).isEqualTo("Experiment[key=42]")
    }

    @Test
    fun `resolved by variation`() {
        // given
        val message = InAppMessages.message(variationKey = "B")
        val messageContext = InAppMessages.messageContext(
            experimentContext = InAppMessage.ExperimentContext(42),
            messages = listOf(message)
        )
        val inAppMessage = InAppMessages.create(messageContext = messageContext)

        val experiment = experiment(id = 5, key = 42)
        val workspace = Workspaces.create(experiments = listOf(experiment))

        val request = InAppMessages.request(inAppMessage = inAppMessage, workspace = workspace)

        val experimentEvaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, emptyList(), experiment, 320, "B", null)
        every { evaluator.evaluate(any(), any()) } returns experimentEvaluation

        val context = Evaluators.context()

        // when
        val actual = sut.resolve(request, context)

        // then
        expectThat(actual).isEqualTo(message)
        expectThat(context.properties).isEqualTo(
            hashMapOf(
                "experiment_id" to 5L,
                "experiment_key" to 42L,
                "variation_id" to 320L,
                "variation_key" to "B",
                "experiment_decision_reason" to "TRAFFIC_ALLOCATED"
            )
        )
        expectThat(context[experiment]).isEqualTo(experimentEvaluation)
    }

    @Test
    fun `resolve`() {
        // given
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.create(
            messageContext = InAppMessages.messageContext(
                defaultLang = "ko",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.request(inAppMessage = inAppMessage)

        // when
        val actual = sut.resolve(request, Evaluators.context())

        // then
        expectThat(actual) isSameInstanceAs message
    }

    @Test
    fun `cannot resolve`() {
        val message = InAppMessages.message(lang = "ko")
        val inAppMessage = InAppMessages.create(
            messageContext = InAppMessages.messageContext(
                defaultLang = "en",
                messages = listOf(message)
            )
        )
        val request = InAppMessages.request(inAppMessage = inAppMessage)

        assertThrows<IllegalArgumentException> {
            sut.resolve(request, Evaluators.context())
        }
    }
}