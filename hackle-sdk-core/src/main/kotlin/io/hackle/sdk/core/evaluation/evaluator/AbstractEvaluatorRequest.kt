package io.hackle.sdk.core.evaluation.evaluator

internal abstract class AbstractEvaluatorRequest : Evaluator.Request {

    final override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Evaluator.Request -> false
            else -> this.key == other.key
        }
    }

    final override fun hashCode(): Int {
        return key.hashCode()
    }
}
