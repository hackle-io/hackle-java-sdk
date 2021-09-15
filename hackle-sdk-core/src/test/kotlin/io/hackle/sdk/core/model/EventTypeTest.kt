package io.hackle.sdk.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class EventTypeTest {

    @Test
    fun `Undefined의 id는 0이다`() {
        val eventType = EventType.Undefined("abc")
        expectThat(eventType) {
            get { key } isEqualTo "abc"
            get { id } isEqualTo 0
        }
    }
}