package io.hackle.sdk.core.evaluation

import io.hackle.sdk.common.User
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.core.evaluation.action.ActionDeterminer
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.workspace.Workspace

internal class Evaluator(
    private val actionDeterminer: ActionDeterminer,
    private val actionResolver: ActionResolver,
) {

    fun evaluate(workspace: Workspace, experiment: Experiment, user: User, defaultVariationKey: String): Evaluation {

        // 1. 수동할당
        val overriddenVariation = experiment.getOverriddenVariationOrNull(user)
        if (overriddenVariation != null) {
            return Evaluation.of(OVERRIDDEN, overriddenVariation.key)
        }

        return when (experiment) {
            is Experiment.Draft -> Evaluation.of(EXPERIMENT_DRAFT, defaultVariationKey)
            is Experiment.Running -> evaluate(workspace, experiment, user, defaultVariationKey)
            is Experiment.Paused -> Evaluation.of(EXPERIMENT_PAUSED, defaultVariationKey)
            is Experiment.Completed -> Evaluation.of(EXPERIMENT_COMPLETED, experiment.winnerVariation.key)
        }
    }

    private fun evaluate(
        workspace: Workspace,
        experiment: Experiment.Running,
        user: User,
        defaultVariationKey: String
    ): Evaluation {

        val action = actionDeterminer.determineOrNull(workspace, experiment, user)
            ?: return Evaluation.of(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)

        val variation = actionResolver.resolveOrNull(action, workspace, experiment, user)
            ?: return Evaluation.of(TRAFFIC_NOT_ALLOCATED, defaultVariationKey)

        if (variation.isDropped) {
            return Evaluation.of(VARIATION_DROPPED, defaultVariationKey)
        }

        return Evaluation.of(TRAFFIC_ALLOCATED, variation.key)
    }
}
