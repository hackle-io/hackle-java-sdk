package io.hackle.sdk.core.evaluation.evaluator.inappmessage.eligibility

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageEligibilityEvaluation(
    override val reason: DecisionReason,
    override val targetEvaluations: List<Evaluator.Evaluation>,
    val inAppMessage: InAppMessage,
    val isEligible: Boolean,
) : Evaluator.Evaluation {

    companion object {

        fun eligible(
            request: InAppMessageEligibilityRequest,
            context: Evaluator.Context,
            reason: DecisionReason,
        ): InAppMessageEligibilityEvaluation {
            return of(request, context, reason, true)
        }

        fun ineligible(
            request: InAppMessageEligibilityRequest,
            context: Evaluator.Context,
            reason: DecisionReason,
        ): InAppMessageEligibilityEvaluation {
            return of(request, context, reason, false)
        }

        fun of(
            request: InAppMessageEligibilityRequest,
            context: Evaluator.Context,
            reason: DecisionReason,
            isEligible: Boolean,
        ): InAppMessageEligibilityEvaluation {
            return InAppMessageEligibilityEvaluation(
                reason = reason,
                targetEvaluations = context.targetEvaluations,
                inAppMessage = request.inAppMessage,
                isEligible = isEligible
            )
        }
    }
}
