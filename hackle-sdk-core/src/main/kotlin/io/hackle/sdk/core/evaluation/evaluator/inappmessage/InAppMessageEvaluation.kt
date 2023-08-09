package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage


internal class InAppMessageEvaluation(
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val inAppMessage: InAppMessage,
    val message: InAppMessage.Message?,
) : Evaluator.Evaluation {

    companion object {
        fun of(
            request: InAppMessageRequest,
            context: Evaluator.Context,
            reason: DecisionReason,
            message: InAppMessage.Message? = null
        ): InAppMessageEvaluation {
            return InAppMessageEvaluation(
                reason = reason,
                targetEvaluations = context.targetEvaluations,
                inAppMessage = request.inAppMessage,
                message = message
            )
        }
    }
}
