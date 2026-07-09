package io.hackle.sdk.core.workspace.config.entity

import io.hackle.sdk.core.model.AbstractInAppMessage
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageConfig(
    override val id: Long,
    override val key: Long,
    override val order: Long,
    override val period: InAppMessage.Period,
    override val timetable: InAppMessage.Timetable,
    override val eventTrigger: InAppMessage.EventTrigger,
    override val evaluateContext: InAppMessage.EvaluateContext,
    override val messageContext: InAppMessage.MessageContext,
    val status: InAppMessage.Status,
    val targetContext: InAppMessage.TargetContext,
) : AbstractInAppMessage(), ConfigEntity {
    override fun toString(): String {
        return "InAppMessageConfig(id=$id, key=$key, status=$status)"
    }
}
