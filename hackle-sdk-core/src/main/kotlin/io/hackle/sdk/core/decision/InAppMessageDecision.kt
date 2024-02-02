package io.hackle.sdk.core.decision

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.model.InAppMessage

data class InAppMessageDecision internal constructor(
    val inAppMessage: InAppMessage?,
    val message: InAppMessage.Message?,
    val reason: DecisionReason,
    val properties: Map<String, Any>,
) {
    val isShow: Boolean get() = inAppMessage != null && message != null

    companion object {
        fun of(
            reason: DecisionReason,
            inAppMessage: InAppMessage? = null,
            message: InAppMessage.Message? = null,
            properties: Map<String, Any> = emptyMap(),
        ): InAppMessageDecision {
            return InAppMessageDecision(inAppMessage, message, reason, properties)
        }
    }
}
