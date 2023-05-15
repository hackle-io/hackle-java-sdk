package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.model.InAppMessage
import java.lang.IllegalArgumentException


internal interface InAppMessageResolver {
    fun resolve(request: InAppMessageRequest, context: Evaluator.Context): InAppMessage.MessageContext.Message
}

internal class DefaultInAppMessageResolver : InAppMessageResolver {

    override fun resolve(
        request: InAppMessageRequest,
        context: Evaluator.Context
    ): InAppMessage.MessageContext.Message {
        val inAppMessage = request.inAppMessage
        return resolveLanguage(inAppMessage)
            ?: throw IllegalArgumentException("InAppMessage must be decided ${inAppMessage.key}")
    }

    private fun resolveLanguage(inAppMessage: InAppMessage): InAppMessage.MessageContext.Message? {
        val defaultLang = inAppMessage.messageContext.defaultLang
        return inAppMessage.messageContext.messages.firstOrNull { it.lang == defaultLang }
    }
}
