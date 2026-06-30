package io.hackle.sdk.core.evaluation.service.remoteconfig.match

import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local.RemoteConfigLocalEvaluateRequest
import io.hackle.sdk.core.model.RemoteConfigParameter

internal class RemoteConfigParameterTargetRuleDeterminer(
    private val matcher: RemoteConfigParameterTargetRuleMatcher,
) {

    fun determine(
        request: RemoteConfigLocalEvaluateRequest<*>,
        context: Evaluator.Context,
    ): RemoteConfigParameter.TargetRule? {
        return request.entity.targetRules.find { matcher.matches(request, context, it) }
    }
}

internal class RemoteConfigParameterTargetRuleMatcher(
    private val targetMatcher: TargetMatcher,
    private val bucketer: Bucketer,
) {
    fun matches(
        request: RemoteConfigLocalEvaluateRequest<*>,
        context: Evaluator.Context,
        rule: RemoteConfigParameter.TargetRule,
    ): Boolean {
        if (!targetMatcher.matches(request, context, rule.target)) {
            return false
        }
        val identifier = request.user.identifiers[request.entity.identifierType] ?: return false
        val bucket = requireNotNull(request.workspace.getBucketOrNull(rule.bucketId)) { "Bucket[${rule.bucketId}]" }
        return bucketer.bucketing(bucket, identifier) != null
    }
}
