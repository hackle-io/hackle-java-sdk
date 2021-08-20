package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.common.User
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.workspace.Workspace

internal class TargetAudienceDeterminer(
    private val targetMatcher: TargetMatcher
) {

    fun isUserInAudiences(workspace: Workspace, experiment: Experiment.Running, user: User): Boolean {
        if (experiment.targetAudiences.isEmpty()) {
            return true
        }
        return experiment.targetAudiences.any { targetMatcher.matches(it, workspace, user) }
    }
}