package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.model.PlatformType

interface InAppMessageEligibilityEvaluateRequest : EvaluateRequest {
    override val entity: InAppMessage
    val scope: InAppMessageEvaluateScope
    val platformType: PlatformType?
    val timestamp: Long
    val inAppMessage: InAppMessage get() = entity
}
