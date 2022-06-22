package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class ExperimentTargetDeterminer(
    private val targetMatcher: TargetMatcher
) {

    fun isUserInExperimentTarget(workspace: Workspace, experiment: Experiment, user: HackleUser): Boolean {
        if (experiment.targetAudiences.isEmpty()) {
            return true
        }
        return experiment.targetAudiences.any { targetMatcher.matches(it, workspace, user) }
    }
}
