package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.model.InAppMessage

internal class InAppMessageUserOverrideDeterminer {

    fun determine(request: InAppMessageRequest): Boolean {
        val overrides = request.inAppMessage.targetContext.overrides

        if (overrides.isEmpty()) {
            return false
        }

        return overrides.any { matches(request, it) }
    }

    private fun matches(request: InAppMessageRequest, userOverride: InAppMessage.TargetContext.UserOverride): Boolean {
        val identifier = request.user.identifiers[userOverride.identifierType] ?: return false
        return userOverride.identifiers.contains(identifier)
    }
}
