package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest

class InAppMessageHiddenMatcher(
    private val storage: InAppMessageHiddenStorage,
) : InAppMessageMatcher<InAppMessageEligibilityEvaluateRequest> {
    override fun matches(
        request: InAppMessageEligibilityEvaluateRequest,
        context: Evaluator.Context,
    ): Boolean {
        return storage.exist(request.inAppMessage, request.timestamp)
    }
}
