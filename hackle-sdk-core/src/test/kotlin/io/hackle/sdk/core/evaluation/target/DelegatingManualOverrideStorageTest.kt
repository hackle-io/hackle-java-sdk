package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.model.Variation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class DelegatingManualOverrideStorageTest {

    @Test
    fun `empty storages`() {
        val sut = DelegatingManualOverrideStorage(emptyList())
        val actual = sut[mockk(), mockk()]
        expectThat(actual).isNull()
    }

    @Test
    fun `first match`() {
        val storage = mockk<ManualOverrideStorage>()
        val variation = mockk<Variation>()
        every { storage[any(), any()] } returnsMany listOf(null, null, null, variation, null)
        val sut = DelegatingManualOverrideStorage(listOf(storage, storage, storage, storage, storage))

        val actual = sut[mockk(), mockk()]

        expectThat(actual) isSameInstanceAs variation
        verify(exactly = 4) {
            storage[any(), any()]
        }
    }

    @Test
    fun `not match`() {
        val storage = mockk<ManualOverrideStorage>()
        every { storage[any(), any()] } returnsMany listOf(null, null, null, null, null)
        val sut = DelegatingManualOverrideStorage(listOf(storage, storage, storage, storage, storage))

        val actual = sut[mockk(), mockk()]

        expectThat(actual).isNull()
        verify(exactly = 5) {
            storage[any(), any()]
        }
    }
}