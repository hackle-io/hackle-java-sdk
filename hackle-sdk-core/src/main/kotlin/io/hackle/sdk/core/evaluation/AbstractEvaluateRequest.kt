package io.hackle.sdk.core.evaluation

abstract class AbstractEvaluateRequest : EvaluateRequest {
    override fun toString(): String {
        return "${javaClass.simpleName}(entity=$entity)"
    }

    final override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is EvaluateRequest -> false
            else -> this.entity == other.entity
        }
    }

    final override fun hashCode(): Int {
        return entity.hashCode()
    }
}
