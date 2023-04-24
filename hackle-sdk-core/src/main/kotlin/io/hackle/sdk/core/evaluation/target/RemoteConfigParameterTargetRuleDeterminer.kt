package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.RemoteConfigParameter

internal class RemoteConfigParameterTargetRuleDeterminer(
    private val matcher: Matcher
) {

    constructor(targetMatcher: TargetMatcher, bucketer: Bucketer) : this(Matcher(targetMatcher, bucketer))

    fun determineTargetRuleOrNull(
        request: RemoteConfigRequest<*>,
        context: Evaluator.Context
    ): RemoteConfigParameter.TargetRule? {
        return request.parameter.targetRules.find { matcher.matches(request, context, it) }
    }

    class Matcher(
        private val targetMatcher: TargetMatcher,
        private val bucketer: Bucketer,
    ) {

        fun matches(
            request: RemoteConfigRequest<*>,
            context: Evaluator.Context,
            targetRule: RemoteConfigParameter.TargetRule,
        ): Boolean {
            if (!targetMatcher.matches(request, context, targetRule.target)) {
                return false
            }
            val identifiers = request.user.identifiers[request.parameter.identifierType] ?: return false
            val bucket =
                requireNotNull(request.workspace.getBucketOrNull(targetRule.bucketId)) { "Bucket[${targetRule.bucketId}]" }
            return bucketer.bucketing(bucket, identifiers) != null
        }
    }
}
