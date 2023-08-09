package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.core.evaluation.evaluator.Evaluator

/**
 * @author Yong
 */
internal sealed class EvaluationFlow<in REQUEST : Evaluator.Request, out EVALUATION : Evaluator.Evaluation> {

    class End<REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> :
        EvaluationFlow<REQUEST, EVALUATION>()

    class Decision<REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation>(
        val flowEvaluator: FlowEvaluator<REQUEST, EVALUATION>,
        val nextFlow: EvaluationFlow<REQUEST, EVALUATION>
    ) : EvaluationFlow<REQUEST, EVALUATION>()

    fun evaluate(request: REQUEST, context: Evaluator.Context): EVALUATION? {
        return when (this) {
            is End<REQUEST, EVALUATION> -> null
            is Decision<REQUEST, EVALUATION> -> flowEvaluator.evaluate(request, context, nextFlow)
        }
    }

    companion object {

        private val END: EvaluationFlow<Evaluator.Request, Evaluator.Evaluation> = End()

        fun <REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> end(): EvaluationFlow<REQUEST, EVALUATION> {
            @Suppress("UNCHECKED_CAST")
            return END as EvaluationFlow<REQUEST, EVALUATION>
        }

        fun <REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> decision(
            evaluator: FlowEvaluator<REQUEST, EVALUATION>,
            nextFlow: EvaluationFlow<REQUEST, EVALUATION>
        ): EvaluationFlow<REQUEST, EVALUATION> {
            return Decision(evaluator, nextFlow)
        }

        fun <REQUEST : Evaluator.Request, EVALUATION : Evaluator.Evaluation> of(
            vararg evaluators: FlowEvaluator<REQUEST, EVALUATION>
        ): EvaluationFlow<REQUEST, EVALUATION> {
            var flow: EvaluationFlow<REQUEST, EVALUATION> = end()
            for (evaluator in evaluators.reversed()) {
                flow = decision(evaluator, flow)
            }
            return flow
        }
    }
}
