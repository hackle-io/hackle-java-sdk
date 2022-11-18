package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class ExperimentTargetRuleDeterminer(
    private val targetMatcher: TargetMatcher
) {

    fun determineTargetRuleOrNull(workspace: Workspace, experiment: Experiment, user: HackleUser): TargetRule? {
        return experiment.targetRules.find { targetMatcher.matches(it.target, workspace, user) }
    }
}
