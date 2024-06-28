package io.hackle.sdk.internal.event

import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent

internal class InMemoryEventProcessor : EventProcessor {

    private val events = mutableListOf<UserEvent>()

    val processedEvents: List<UserEvent> get() = ArrayList(events)

    override fun process(event: UserEvent) {
        events.add(event)
    }

    override fun flush() {
        events.clear()
    }
}
