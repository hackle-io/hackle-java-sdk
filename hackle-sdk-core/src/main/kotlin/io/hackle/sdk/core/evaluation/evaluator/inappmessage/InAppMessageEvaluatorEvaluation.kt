package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage

abstract class InAppMessageEvaluatorEvaluation : Evaluator.Evaluation {
    abstract override val reason: DecisionReason
    abstract override val targetEvaluations: List<Evaluator.Evaluation>
    abstract val inAppMessage: InAppMessage
}
