package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.model.TargetRule
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class TargetRuleDeterminer(
    private val targetMatcher: TargetMatcher,
    private val bucketer: Bucketer
) {

    fun determineTargetRuleOrNull(workspace: Workspace, experiment: Experiment, user: HackleUser): TargetRule? {
        return experiment.targetRules.find { targetMatcher.matches(it.target, workspace, user) }
    }

    fun determineTargetRuleOrNull(
        workspace: Workspace,
        parameter: RemoteConfigParameter,
        user: HackleUser
    ): RemoteConfigParameter.TargetRule? {
        return parameter.targetRules.find { matches(it, workspace, parameter, user) }
    }

    private fun matches(
        targetRule: RemoteConfigParameter.TargetRule,
        workspace: Workspace,
        parameter: RemoteConfigParameter,
        user: HackleUser
    ): Boolean {
        if (!targetMatcher.matches(targetRule.target, workspace, user)) {
            return false
        }
        val identifiers = user.identifiers[parameter.identifierType] ?: return false
        val bucket = requireNotNull(workspace.getBucketOrNull(targetRule.bucketId))
        return bucketer.bucketing(bucket, identifiers) != null
    }
}
