package io.hackle.sdk.core.evaluation.service.inappmessage.layout.match

import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.workspace.config.entity.InAppMessageConfig

class InAppMessageLayoutSelector {
    fun select(inAppMessage: InAppMessageConfig, condition: (InAppMessage.Message) -> Boolean): InAppMessage.Message {
        val message = inAppMessage.messageContext.messages.find(condition)
        return requireNotNull(message) { "InAppMessage must be decided [${inAppMessage.id}]" }
    }
}
