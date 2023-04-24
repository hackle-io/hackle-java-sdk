package io.hackle.sdk.core.event

internal fun EventProcessor.process(events: List<UserEvent>) {
    for (event in events) {
        process(event)
    }
}
