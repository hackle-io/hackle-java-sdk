package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage


internal class InAppMessageEvaluation(
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val message: InAppMessage.MessageContext.Message?,
) : Evaluator.Evaluation {

    companion object {
        fun of(
            reason: DecisionReason,
            context: Evaluator.Context,
            message: InAppMessage.MessageContext.Message? = null
        ): InAppMessageEvaluation {
            return InAppMessageEvaluation(
                reason = reason,
                targetEvaluations = context.targetEvaluations,
                message = message
            )
        }
    }
}
