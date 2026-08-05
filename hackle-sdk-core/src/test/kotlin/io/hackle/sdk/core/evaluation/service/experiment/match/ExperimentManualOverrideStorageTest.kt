package io.hackle.sdk.core.evaluation.service.experiment.match

import io.hackle.sdk.core.support.Experiments
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.user.IdentifierType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNull
import strikt.assertions.isSameInstanceAs

internal class ExperimentManualOverrideStorageTest {

    private val experiment = Experiments.config()
    private val user = HackleUser.builder().identifier(IdentifierType.ID, "user").build()

    @Nested
    inner class DelegatingExperimentManualOverrideStorageTest {

        @Test
        fun `empty storages`() {
            val sut = DelegatingExperimentManualOverrideStorage(emptyList())

            val actual = sut[experiment, user]

            expectThat(actual).isNull()
        }

        @Test
        fun `first match`() {
            val storage = mockk<ExperimentManualOverrideStorage>()
            val variation = Experiments.variation()
            every { storage[any(), any()] } returnsMany listOf(null, null, null, variation, null)
            val sut = DelegatingExperimentManualOverrideStorage(listOf(storage, storage, storage, storage, storage))

            val actual = sut[experiment, user]

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

            val actual = sut[experiment, user]

            expectThat(actual).isNull()
            verify(exactly = 5) {
                storage[any(), any()]
            }
        }
    }

    @Nested
    inner class NoopExperimentManualOverrideStorageTest {

        @Test
        fun `always returns null`() {
            expectThat(NoopExperimentManualOverrideStorage[experiment, user]).isNull()
        }
    }
}
