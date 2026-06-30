package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.service.experiment.match.DelegatingExperimentManualOverrideStorage
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentManualOverrideStorage
import io.hackle.sdk.core.model.Variation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class DelegatingExperimentManualOverrideStorageTest {

    @Test
    fun `empty storages`() {
        val sut = DelegatingExperimentManualOverrideStorage(emptyList())
        val actual = sut[mockk(), mockk()]
        expectThat(actual).isNull()
    }

    @Test
    fun `first match`() {
        val storage = mockk<ExperimentManualOverrideStorage>()
        val variation = mockk<Variation>()
        every { storage[any(), any()] } returnsMany listOf(null, null, null, variation, null)
        val sut = DelegatingExperimentManualOverrideStorage(listOf(storage, storage, storage, storage, storage))

        val actual = sut[mockk(), mockk()]

        expectThat(actual) isSameInstanceAs variation
        verify(exactly = 4) {
            storage[any(), any()]
        }
    }

    @Test
    fun `not match`() {
        val storage = mockk<ExperimentManualOverrideStorage>()
        every { storage[any(), any()] } returnsMany listOf(null, null, null, null, null)
        val sut = DelegatingExperimentManualOverrideStorage(listOf(storage, storage, storage, storage, storage))

        val actual = sut[mockk(), mockk()]

        expectThat(actual).isNull()
        verify(exactly = 5) {
            storage[any(), any()]
        }
    }
}
