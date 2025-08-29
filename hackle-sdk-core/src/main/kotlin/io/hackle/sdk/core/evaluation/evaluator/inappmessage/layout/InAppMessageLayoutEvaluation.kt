package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageEvaluatorEvaluation
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutEvaluation(
    val request: InAppMessageLayoutRequest,
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val message: InAppMessage.Message,
    val properties: Map<String, Any>,
) : InAppMessageEvaluatorEvaluation() {

    override val inAppMessage: InAppMessage get() = request.inAppMessage

    companion object {
        fun of(
            request: InAppMessageLayoutRequest,
            context: Evaluator.Context,
            message: InAppMessage.Message,
        ): InAppMessageLayoutEvaluation {
            return InAppMessageLayoutEvaluation(
                request = request,
                reason = DecisionReason.IN_APP_MESSAGE_TARGET,
                targetEvaluations = context.targetEvaluations,
                message = message,
                properties = context.properties
            )
        }
    }
}
