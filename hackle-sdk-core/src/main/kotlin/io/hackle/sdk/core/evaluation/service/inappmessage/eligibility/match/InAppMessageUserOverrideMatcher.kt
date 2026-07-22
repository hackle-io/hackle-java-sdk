package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluateRequest
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageUserOverrideMatcher : InAppMessageMatcher<InAppMessageEligibilityLocalEvaluateRequest> {
    override fun matches(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        context: Evaluator.Context,
    ): Boolean {
        return request.entity.targetContext.overrides.any { isUserOverridden(request, it) }
    }

    private fun isUserOverridden(
        request: InAppMessageEligibilityLocalEvaluateRequest,
        userOverride: InAppMessage.UserOverride,
    ): Boolean {
        val identifier = request.user.identifiers[userOverride.identifierType] ?: return false
        return identifier in userOverride.identifiers
    }
}
