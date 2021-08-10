package io.hackle.sdk.core.evaluation.action

import io.hackle.sdk.common.User
import io.hackle.sdk.core.evaluation.match.ExperimentRuleMatcher
import io.hackle.sdk.core.model.Action
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.workspace.Workspace

internal class ActionDeterminer(
    private val ruleMatcher: ExperimentRuleMatcher,
) {

    fun determineOrNull(workspace: Workspace, experiment: Experiment.Running, user: User): Action? {
        return when (experiment.type) {
            Experiment.Type.AB_TEST -> determineAbTest(workspace, experiment, user)
            Experiment.Type.FEATURE_FLAG -> determineFeatureFlag(workspace, experiment, user)
        }
    }

    private fun determineAbTest(workspace: Workspace, experiment: Experiment.Running, user: User): Action? {
        if (experiment.rules.isEmpty()) {
            return experiment.defaultAction
        }
        val matchedRule = ruleMatcher.matches(workspace, experiment, user)
        return matchedRule?.action
    }

    private fun determineFeatureFlag(workspace: Workspace, experiment: Experiment.Running, user: User): Action {
        val matchedRule = ruleMatcher.matches(workspace, experiment, user)
        return matchedRule?.action ?: experiment.defaultAction
    }
}
