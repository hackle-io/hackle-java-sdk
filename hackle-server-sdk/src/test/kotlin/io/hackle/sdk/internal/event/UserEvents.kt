package io.hackle.sdk.internal.event

import io.hackle.sdk.common.Event
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import java.util.*

object UserEvents {

    fun track(key: String): UserEvent.Track {
        return track(eventType = EventType.Custom(42, key), event = Event.of(key))
    }

    fun track(
        insertId: String = UUID.randomUUID().toString(),
        timestamp: Long = 42,
        user: HackleUser = HackleUser.builder().identifier(IdentifierType.ID, "user").build(),
        eventType: EventType = EventType.Custom(1, "test_key"),
        event: Event = Event.of("test_key")
    ): UserEvent.Track {
        return UserEvent.Track(insertId, timestamp, user, eventType, event)
    }
}