package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest

internal class InAppMessageUserOverrideDeterminer {

    fun determine(request: InAppMessageRequest): Boolean {
        val overrides = request.inAppMessage.targetContext.overrides

        if (overrides.isEmpty()) {
            return false
        }

        return overrides.any {
            val identifier = request.user.identifiers[it.identifierType] ?: return false
            return it.identifiers.contains(identifier)
        }
    }
}
