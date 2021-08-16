package io.hackle.sdk.core.evaluation.rule

import io.hackle.sdk.common.User
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.workspace.Workspace

internal class ExperimentTargetRuleMatcher(
    private val targetMatcher: TargetMatcher
) {

    fun matchesOrNull(workspace: Workspace, experiment: Experiment.Running, user: User): TargetRule? {
        return experiment.targetRules.find { targetMatcher.matches(it.target, workspace, user) }
    }
}
