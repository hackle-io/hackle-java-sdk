package io.hackle.sdk.core.evaluation.service.inappmessage.layout

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.InAppMessageEvaluateScope
import io.hackle.sdk.core.model.InAppMessage

interface InAppMessageLayoutEvaluateRequest : EvaluateRequest {
    override val entity: InAppMessage
    val scope: InAppMessageEvaluateScope
}
