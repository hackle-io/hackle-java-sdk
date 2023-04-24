package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason.TRAFFIC_NOT_ALLOCATED
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest

/**
 * @author Yong
 */
internal sealed class EvaluationFlow {

    fun evaluate(request: ExperimentRequest, context: Evaluator.Context): ExperimentEvaluation {
        return when (this) {
            is End -> ExperimentEvaluation.ofDefault(request, context, TRAFFIC_NOT_ALLOCATED)
            is Decision -> flowEvaluator.evaluate(request, context, nextFlow)
        }
    }

    object End : EvaluationFlow()
    class Decision(val flowEvaluator: FlowEvaluator, val nextFlow: EvaluationFlow) : EvaluationFlow()

    companion object {
        fun of(vararg evaluators: FlowEvaluator): EvaluationFlow {
            var flow: EvaluationFlow = End
            val iterator = evaluators.toList().listIterator(evaluators.size)
            while (iterator.hasPrevious()) {
                flow = Decision(iterator.previous(), flow)
            }
            return flow
        }
    }
}
