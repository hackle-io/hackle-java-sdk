package io.hackle.sdk.core.evaluation.evaluator.experiment

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.model.InAppMessages
import io.hackle.sdk.core.model.experiment
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isSameInstanceAs

@ExtendWith(MockKExtension::class)
class ExperimentContextualEvaluatorTest {

    @MockK
    private lateinit var evaluator: Evaluator

    @InjectMockKs
    private lateinit var sut: TestExperimentContextualEvaluator

    @Test
    fun `이미 평가된 경우 다시 평가하지 않고 평가된 결과를 리턴한다`() {
        // given
        val request = InAppMessages.layoutRequest()
        val experiment = experiment()
        val experimentEvaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, emptyList(), experiment, 0, "A", null)
        val context = Evaluators.context()
        context.add(experimentEvaluation)

        // when
        val actual = sut.evaluate(request, context, experiment)

        // then
        expectThat(actual) isSameInstanceAs experimentEvaluation
        verify(exactly = 0) {
            evaluator.evaluate(any(), any())
        }
    }

    @Test
    fun `평가결과 ExperimentEvaluation이 아니면 예외 발생`() {
        val request = InAppMessages.layoutRequest()
        val context = Evaluators.context()
        val experiment = experiment()

        val evaluation = InAppMessages.evaluation()
        every { evaluator.evaluate(any(), any()) } returns evaluation

        assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context, experiment)
        }
    }

    @Test
    fun `평가후 resolve 호출`() {
        // given
        val request = InAppMessages.layoutRequest()
        val context = Evaluators.context()
        val experiment = experiment()

        val experimentEvaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, emptyList(), experiment, 0, "A", null)
        every { evaluator.evaluate(any(), any()) } returns experimentEvaluation

        // when
        val actual = sut.evaluate(request, context, experiment)

        // then
        expectThat(actual) isSameInstanceAs experimentEvaluation
    }

    @Test
    fun `context 에 평가 결과를 추가한다`() {
        // given
        val request = InAppMessages.layoutRequest()
        val context = Evaluators.context()
        val experiment = experiment()

        val experimentEvaluation =
            ExperimentEvaluation(DecisionReason.TRAFFIC_ALLOCATED, emptyList(), experiment, 0, "A", null)
        every { evaluator.evaluate(any(), any()) } returns experimentEvaluation

        // when
        val actual = sut.evaluate(request, context, experiment)

        // then
        expectThat(context[experiment]) isSameInstanceAs actual
    }

    class TestExperimentContextualEvaluator(override val evaluator: Evaluator) : ExperimentContextualEvaluator() {
        override fun resolve(
            request: Evaluator.Request,
            context: Evaluator.Context,
            evaluation: ExperimentEvaluation,
        ): ExperimentEvaluation {

            context.setProperty("hello", "evaluation")
            return evaluation
        }
    }
}
