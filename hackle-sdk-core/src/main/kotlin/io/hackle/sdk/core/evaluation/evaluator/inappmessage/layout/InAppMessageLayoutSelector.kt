package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutSelector {
    fun select(inAppMessage: InAppMessage, condition: (InAppMessage.Message) -> Boolean): InAppMessage.Message {
        val message = inAppMessage.messageContext.messages.find(condition)
        return requireNotNull(message) { "InAppMessage must be decided [${inAppMessage.id}]" }
    }
}
