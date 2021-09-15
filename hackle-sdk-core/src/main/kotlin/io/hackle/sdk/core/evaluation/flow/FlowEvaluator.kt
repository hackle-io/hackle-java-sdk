package io.hackle.sdk.core.evaluation.flow

import io.hackle.sdk.common.User
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal interface FlowEvaluator {
    fun evaluate(
        workspace: Workspace,
        experiment: Experiment,
        user: User,
        defaultVariationKey: String,
        nextFlow: EvaluationFlow
    ): Evaluation
}