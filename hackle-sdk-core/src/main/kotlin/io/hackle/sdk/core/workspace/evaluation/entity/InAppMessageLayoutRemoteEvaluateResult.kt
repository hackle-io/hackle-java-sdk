package io.hackle.sdk.core.workspace.evaluation.entity

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.Evaluation
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluation
import io.hackle.sdk.core.model.AbstractInAppMessage
import io.hackle.sdk.core.model.Entity
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutRemoteEvaluateResult(
    override val id: Long,
    override val key: Long,
    override val period: InAppMessage.Period,
    override val timetable: InAppMessage.Timetable,
    override val eventTrigger: InAppMessage.EventTrigger,
    override val evaluateContext: InAppMessage.EvaluateContext,
    override val messageContext: InAppMessage.MessageContext,
    override val message: InAppMessage.Message,
    override val reason: DecisionReason,
    override val references: List<Entity>,
) : AbstractInAppMessage(),
    InAppMessageLayoutEvaluateResult,
    RemoteEvaluateResult {
    override fun toEvaluation(): Evaluation {
        return InAppMessageLayoutEvaluation(this, this)
    }
}
