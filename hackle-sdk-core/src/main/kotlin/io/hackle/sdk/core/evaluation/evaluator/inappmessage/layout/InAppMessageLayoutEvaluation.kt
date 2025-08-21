package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutEvaluation(
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val inAppMessage: InAppMessage,
    val message: InAppMessage.Message,
    val properties: Map<String, Any>,
) : Evaluator.Evaluation {

    companion object {
        fun of(
            request: InAppMessageLayoutRequest,
            context: Evaluator.Context,
            message: InAppMessage.Message,
        ): InAppMessageLayoutEvaluation {
            return InAppMessageLayoutEvaluation(
                reason = DecisionReason.IN_APP_MESSAGE_TARGET,
                targetEvaluations = context.targetEvaluations,
                inAppMessage = request.inAppMessage,
                message = message,
                properties = context.properties
            )
        }
    }
}
