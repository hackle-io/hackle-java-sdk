package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.InAppMessage


internal class InAppMessageResolver {
    fun resolve(request: InAppMessageRequest, context: Evaluator.Context): InAppMessage.Message {
        val inAppMessage = request.inAppMessage
        val lang = inAppMessage.messageContext.defaultLang
        return inAppMessage.messageContext.messages.find { it.lang == lang }
            ?: throw IllegalArgumentException("InAppMessage must be decided [${inAppMessage.key}]")
    }
}

internal interface InAppMessageMatcher {
    fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean
}

internal class InAppMessageUserOverrideMatcher : InAppMessageMatcher {
    override fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        return request.inAppMessage.targetContext.overrides.any { isUserOverridden(request, it) }
    }

    private fun isUserOverridden(request: InAppMessageRequest, userOverride: InAppMessage.UserOverride): Boolean {
        val identifier = request.user.identifiers[userOverride.identifierType] ?: return false
        return identifier in userOverride.identifiers
    }
}

internal class InAppMessageTargetMatcher(
    private val targetMatcher: TargetMatcher
) : InAppMessageMatcher {
    override fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        return targetMatcher.anyMatches(request, context, request.inAppMessage.targetContext.targets)
    }
}

internal class InAppMessageHiddenMatcher(
    private val storage: InAppMessageHiddenStorage
) : InAppMessageMatcher {
    override fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        return storage.exist(request.inAppMessage, request.timestamp)
    }
}
