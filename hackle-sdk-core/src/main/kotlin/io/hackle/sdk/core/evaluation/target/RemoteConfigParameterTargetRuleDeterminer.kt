package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.RemoteConfigParameter
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.Workspace

internal class RemoteConfigParameterTargetRuleDeterminer(
    private val matcher: Matcher
) {

    constructor(targetMatcher: TargetMatcher, bucketer: Bucketer) : this(Matcher(targetMatcher, bucketer))

    fun determineTargetRuleOrNull(
        workspace: Workspace,
        parameter: RemoteConfigParameter,
        user: HackleUser
    ): RemoteConfigParameter.TargetRule? {
        return parameter.targetRules.find { matcher.matches(it, workspace, parameter, user) }
    }

    class Matcher(
        private val targetMatcher: TargetMatcher,
        private val bucketer: Bucketer,
    ) {

        fun matches(
            targetRule: RemoteConfigParameter.TargetRule,
            workspace: Workspace,
            parameter: RemoteConfigParameter,
            user: HackleUser
        ): Boolean {
            if (!targetMatcher.matches(targetRule.target, workspace, user)) {
                return false
            }
            val identifiers = user.identifiers[parameter.identifierType] ?: return false
            val bucket =
                requireNotNull(workspace.getBucketOrNull(targetRule.bucketId)) { "Bucket[${targetRule.bucketId}]" }
            return bucketer.bucketing(bucket, identifiers) != null
        }
    }
}
