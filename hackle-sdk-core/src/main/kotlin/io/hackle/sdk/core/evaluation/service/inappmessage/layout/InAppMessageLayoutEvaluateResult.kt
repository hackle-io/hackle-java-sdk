package io.hackle.sdk.core.evaluation.service.inappmessage.layout

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateResult
import io.hackle.sdk.core.model.InAppMessage

interface InAppMessageLayoutEvaluateResult : EvaluateResult {
    val message: InAppMessage.Message

    companion object {
        fun of(reason: DecisionReason, message: InAppMessage.Message): InAppMessageLayoutEvaluateResult {
            return DefaultInAppMessageLayoutEvaluateResult(reason = reason, message = message)
        }
    }
}

private class DefaultInAppMessageLayoutEvaluateResult(
    override val reason: DecisionReason,
    override val message: InAppMessage.Message,
) : InAppMessageLayoutEvaluateResult
