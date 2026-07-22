package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluateRequest
import io.hackle.sdk.core.support.Experiments
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isSameInstanceAs
import strikt.assertions.startsWith

internal class ContextualEvaluatorTest {

    @Test
    fun `doEvaluate 결과를 리턴한다`() {
        // given
        val response = Experiments.response()
        val sut = TestContextualEvaluator { _, _ -> response }

        // when
        val actual = sut.evaluate(Experiments.localRequest(), Evaluators.context())

        // then
        expectThat(actual) isSameInstanceAs response
    }

    @Test
    fun `평가 중에는 request 가 context 에 추가되어 있다`() {
        // given
        val request = Experiments.localRequest()
        var stackOnEvaluate: List<EvaluateRequest>? = null
        val sut = TestContextualEvaluator { _, context ->
            stackOnEvaluate = context.stack
            Experiments.response()
        }

        // when
        sut.evaluate(request, Evaluators.context())

        // then
        expectThat(stackOnEvaluate).isNotNull().containsExactly(request)
    }

    @Test
    fun `평가가 끝나면 request 를 context 에서 제거한다`() {
        // given
        val request = Experiments.localRequest()
        val context = Evaluators.context()
        val sut = TestContextualEvaluator { _, _ -> Experiments.response() }

        // when
        sut.evaluate(request, context)

        // then
        expectThat(request in context).isFalse()
        expectThat(context.stack).hasSize(0)
    }

    @Test
    fun `평가 중 예외가 발생해도 request 를 context 에서 제거한다`() {
        // given
        val request = Experiments.localRequest()
        val context = Evaluators.context()
        val sut = TestContextualEvaluator { _, _ -> throw IllegalStateException("evaluate failed") }

        // when
        assertThrows<IllegalStateException> {
            sut.evaluate(request, context)
        }

        // then
        expectThat(request in context).isFalse()
        expectThat(context.stack).hasSize(0)
    }

    @Test
    fun `이미 평가 중인 request 면 순환 평가로 예외 발생`() {
        // given
        val request = Experiments.localRequest()
        val context = Evaluators.context()
        context.add(request)
        val sut = TestContextualEvaluator { _, _ -> Experiments.response() }

        // when
        val exception = assertThrows<IllegalArgumentException> {
            sut.evaluate(request, context)
        }

        // then
        expectThat(exception.message).isNotNull().startsWith("Circular evaluation has occurred")
    }

    private class TestContextualEvaluator(
        private val onEvaluate: (ExperimentLocalEvaluateRequest, Evaluator.Context) -> ExperimentEvaluateResponse,
    ) : ContextualEvaluator<ExperimentLocalEvaluateRequest, ExperimentEvaluateResponse>() {

        override fun supports(request: EvaluateRequest): Boolean {
            return true
        }

        override fun doEvaluate(
            request: ExperimentLocalEvaluateRequest,
            context: Evaluator.Context,
        ): ExperimentEvaluateResponse {
            return onEvaluate(request, context)
        }

        override fun record(request: ExperimentLocalEvaluateRequest, response: ExperimentEvaluateResponse) {
        }
    }
}
