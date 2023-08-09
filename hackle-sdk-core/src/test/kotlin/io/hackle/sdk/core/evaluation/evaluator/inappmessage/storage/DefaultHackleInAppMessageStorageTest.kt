package io.hackle.sdk.core.evaluation.evaluator.inappmessage.storage

import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse

internal class DefaultHackleInAppMessageStorageTest {

    private val sut = DefaultHackleInAppMessageStorage()

    @Test
    fun `exist returns false`() {
        expectThat(sut.exist(mockk(), 42)).isFalse()
    }

    @Test
    fun `put do nothing`() {
        sut.put(mockk(), 42)
    }
}