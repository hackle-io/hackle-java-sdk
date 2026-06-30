package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.evaluation.evaluator.Evaluator

sealed class EvaluationFlow<REQUEST : EvaluateRequest, RESULT : EvaluateResult> {

    class End<REQUEST : EvaluateRequest, RESULT : EvaluateResult> : EvaluationFlow<REQUEST, RESULT>()

    class Step<REQUEST : EvaluateRequest, RESULT : EvaluateResult>(
        val flowEvaluator: FlowEvaluator<REQUEST, RESULT>,
        val nextFlow: EvaluationFlow<REQUEST, RESULT>,
    ) : EvaluationFlow<REQUEST, RESULT>()

    fun evaluate(request: REQUEST, context: Evaluator.Context): RESULT? {
        return when (this) {
            is End<REQUEST, RESULT> -> null
            is Step<REQUEST, RESULT> -> flowEvaluator.evaluate(request, context, nextFlow)
        }
    }

    operator fun plus(flow: EvaluationFlow<REQUEST, RESULT>): EvaluationFlow<REQUEST, RESULT> {
        return when (this) {
            is End -> flow
            is Step -> Step(flowEvaluator, nextFlow + flow)
        }
    }

    companion object {

        private val END: EvaluationFlow<EvaluateRequest, EvaluateResult> = End()

        fun <REQUEST : EvaluateRequest, RESULT : EvaluateResult> end(): EvaluationFlow<REQUEST, RESULT> {
            @Suppress("UNCHECKED_CAST")
            return END as EvaluationFlow<REQUEST, RESULT>
        }

        fun <REQUEST : EvaluateRequest, RESULT : EvaluateResult> decision(
            evaluator: FlowEvaluator<REQUEST, RESULT>,
            nextFlow: EvaluationFlow<REQUEST, RESULT>,
        ): EvaluationFlow<REQUEST, RESULT> {
            return Step(evaluator, nextFlow)
        }

        fun <REQUEST : EvaluateRequest, RESULT : EvaluateResult> of(
            vararg evaluators: FlowEvaluator<REQUEST, RESULT>,
        ): EvaluationFlow<REQUEST, RESULT> {
            var flow: EvaluationFlow<REQUEST, RESULT> = end()
            for (evaluator in evaluators.reversed()) {
                flow = decision(evaluator, flow)
            }
            return flow
        }
    }
}
