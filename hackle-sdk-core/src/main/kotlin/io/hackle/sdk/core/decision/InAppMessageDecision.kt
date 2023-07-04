package io.hackle.sdk.core.decision

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.InAppMessage

data class InAppMessageDecision(
    val reason: DecisionReason,
    val inAppMessage: InAppMessage? = null,
    val message: InAppMessage.MessageContext.Message? = null,
) {
    val isShow: Boolean get() = inAppMessage != null && message != null
}
