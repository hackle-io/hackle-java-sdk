package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.User
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class Evaluator(
    private val evaluationFlowFactory: EvaluationFlowFactory
) {
    fun evaluate(workspace: Workspace, experiment: Experiment, user: User, defaultVariationKey: String): Evaluation {
        val evaluationFlow = evaluationFlowFactory.getFlow(experiment.type)
        return evaluationFlow.evaluate(workspace, experiment, user, defaultVariationKey)
    }
}
