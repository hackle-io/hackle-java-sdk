package io.hackle.sdk.core.event

/**
 * @author Yong
 */
interface EventProcessor {
    fun process(event: UserEvent)
}
