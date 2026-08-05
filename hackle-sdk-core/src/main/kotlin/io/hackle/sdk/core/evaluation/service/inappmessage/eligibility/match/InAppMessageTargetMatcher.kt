package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluateRequest

class InAppMessageTargetMatcher(
    private val targetMatcher: TargetMatcher,
) : InAppMessageMatcher<InAppMessageEligibilityLocalEvaluateRequest> {
    override fun matches(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
    ): Boolean {
        return targetMatcher.anyMatches(request, context, request.entity.targetContext.targets)
    }
}
