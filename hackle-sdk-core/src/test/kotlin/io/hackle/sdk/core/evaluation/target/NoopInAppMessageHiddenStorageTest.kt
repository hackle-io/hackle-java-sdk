package io.hackle.sdk.core.evaluation.target

import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse

internal class NoopInAppMessageHiddenStorageTest {

    @Test
    fun `exist returns false`() {
        expectThat(NoopInAppMessageHiddenStorage.exist(mockk(), 42)).isFalse()
    }

    @Test
    fun `put do nothing`() {
        NoopInAppMessageHiddenStorage.put(mockk(), 42)
    }
}