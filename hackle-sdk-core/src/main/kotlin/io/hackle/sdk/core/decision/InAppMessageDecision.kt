package io.hackle.sdk.core.decision

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.InAppMessage

data class InAppMessageDecision(
    val reason: DecisionReason,
    val isShow: Boolean,
    val inAppMessage: InAppMessage? = null,
    val message: InAppMessage.MessageContext.Message? = null,
)
