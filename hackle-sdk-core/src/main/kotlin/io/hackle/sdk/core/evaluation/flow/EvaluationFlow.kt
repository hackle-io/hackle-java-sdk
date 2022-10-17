package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal sealed class EvaluationFlow {

    fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: HackleUser,
        defaultVariationKey: String
    ): Evaluation {
        return when (this) {
            is End -> Evaluation.of(workspace, experiment, defaultVariationKey, DecisionReason.TRAFFIC_NOT_ALLOCATED)
            is Decision -> flowEvaluator.evaluate(workspace, experiment, user, defaultVariationKey, nextFlow)
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
