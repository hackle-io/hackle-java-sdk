package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.common.User
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.workspace.Workspace

internal class ExperimentRuleMatcher(
    private val targetMatcher: TargetMatcher
) {

    /**
     * Find the first matching rule from the tar
     */
    fun matches(workspace: Workspace, experiment: Experiment.Running, user: User): TargetRule? {
        return experiment.rules.find { targetMatcher.matches(it.target, workspace, user) }
    }
}
