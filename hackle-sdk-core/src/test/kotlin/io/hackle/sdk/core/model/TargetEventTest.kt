package io.hackle.sdk.core.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

internal class TargetEventTest {

    @Test
    fun `is equal`() {
        val targetEvent = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 2)))
        val otherEvent = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 3)))

        val targetEvent2 = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 2)), property = TargetEvent.Property("key", Target.Key.Type.EVENT_PROPERTY, "value"))
        val otherEvent2 = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 3)), property = TargetEvent.Property("key", Target.Key.Type.EVENT_PROPERTY, "value"))

        expectThat(targetEvent == otherEvent).isTrue()
        expectThat(targetEvent2 == otherEvent2).isTrue()
        expectThat(targetEvent.hashCode() == otherEvent.hashCode()).isTrue()
        expectThat(targetEvent2.hashCode() == otherEvent2.hashCode()).isTrue()
    }

    @Test
    fun `is not equal`() {
        val targetEvent = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 2)))
        val otherEvent = TargetEvent("otherEventKey", listOf(TargetEvent.Stat(1, 2)))

        val targetEvent2 = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 2)), property = TargetEvent.Property("key", Target.Key.Type.EVENT_PROPERTY, "value"))
        val otherEvent2 = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 2)), property = TargetEvent.Property("key", Target.Key.Type.EVENT_PROPERTY, "value2"))

        expectThat(targetEvent == otherEvent).isFalse()
        expectThat(targetEvent2 == otherEvent2).isFalse()
        expectThat(targetEvent.hashCode() == otherEvent.hashCode()).isFalse()
        expectThat(targetEvent2.hashCode() == otherEvent2.hashCode()).isFalse()

        val targetEvent3 = TargetEvent("eventKey", listOf(TargetEvent.Stat(1, 2)), property = TargetEvent.Property("key", Target.Key.Type.EVENT_PROPERTY, "value"))
        val otherAny = Any()

        expectThat(targetEvent3 == otherAny).isFalse()
    }


}