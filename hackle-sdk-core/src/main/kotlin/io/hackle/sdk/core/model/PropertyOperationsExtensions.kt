package io.hackle.sdk.core.model

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.PropertyOperations

fun PropertyOperations.toEvent(): Event {
    val builder = Event.builder("\$properties")
    for ((operation, properties) in asMap()) {
        builder.property(operation.key, properties)
    }
    return builder.build()
}
