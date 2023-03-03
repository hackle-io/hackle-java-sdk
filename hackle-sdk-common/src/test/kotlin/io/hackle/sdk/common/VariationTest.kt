package io.hackle.sdk.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class VariationTest {


    @Test
    fun `CONTROL`() {
        expectThat(Variation.CONTROL) isEqualTo Variation.A
    }

    @Test
    fun `from`() {
        fun verify(variation: Variation, vararg keys: String) {
            for (key in keys) {
                expectThat(Variation.from(key)) isEqualTo variation
            }

        }

        verify(Variation.A, "a", "A")
        verify(Variation.B, "b", "B")
        verify(Variation.C, "c", "C")
        verify(Variation.D, "d", "D")
        verify(Variation.E, "e", "E")
        verify(Variation.F, "f", "F")
        verify(Variation.G, "g", "G")
        verify(Variation.H, "h", "H")
        verify(Variation.I, "i", "I")
        verify(Variation.J, "j", "J")

        assertThrows<IllegalArgumentException> { Variation.from("k") }

        expectThat(Variation.fromOrControl("b")) isEqualTo Variation.B
        expectThat(Variation.fromOrControl("k")) isEqualTo Variation.A
    }
}