package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest

interface InAppMessageMatcher<in REQUEST : InAppMessageEligibilityEvaluateRequest> {
    fun matches(request: REQUEST, context: Evaluator.Context): Boolean
}
