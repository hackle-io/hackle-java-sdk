package io.hackle.sdk.core.evaluation.evaluator.inappmessage

import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator

abstract class InAppMessageEvaluator<in REQUEST : InAppMessageEvaluatorRequest, EVALUATION : InAppMessageEvaluatorEvaluation> :
    ContextualEvaluator<REQUEST, EVALUATION>() {
    abstract fun record(request: REQUEST, evaluation: EVALUATION)
}
