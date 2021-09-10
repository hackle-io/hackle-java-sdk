package io.hackle.sdk.core.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SlotTest {

    @Test
    fun `contains`() {
        val slot = Slot(1, 2, 3)
        assertFalse(slot.contains(0))
        assertTrue(slot.contains(1))
        assertFalse(slot.contains(2))
        assertFalse(slot.contains(4))
    }
}