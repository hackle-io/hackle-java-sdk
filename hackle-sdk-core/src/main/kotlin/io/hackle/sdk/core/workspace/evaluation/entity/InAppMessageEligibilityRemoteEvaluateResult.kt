package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluation
import io.hackle.sdk.core.model.AbstractInAppMessage
import io.hackle.sdk.core.model.Entity
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageEligibilityRemoteEvaluateResult(
    override val id: Long,
    override val key: Long,
    override val order: Long,
    override val period: InAppMessage.Period,
    override val timetable: InAppMessage.Timetable,
    override val eventTrigger: InAppMessage.EventTrigger,
    override val evaluateContext: InAppMessage.EvaluateContext,
    override val messageContext: InAppMessage.MessageContext,
    override val isEligible: Boolean,
    override val reason: DecisionReason,
    override val references: List<Entity>,
    val layout: InAppMessageLayoutRemoteEvaluateResult,
) : AbstractInAppMessage(),
    InAppMessageEligibilityEvaluateResult,
    RemoteEvaluateResult {
    override fun toEvaluation(): Evaluation {
        return InAppMessageEligibilityEvaluation(this, this)
    }
}
