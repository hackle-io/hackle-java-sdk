package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.support.InAppMessages
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse

internal class NoopInAppMessageHiddenStorageTest {

    @Test
    fun `exist returns false`() {
        expectThat(NoopInAppMessageHiddenStorage.exist(InAppMessages.config(), 42)).isFalse()
    }

    @Test
    fun `put do nothing`() {
        NoopInAppMessageHiddenStorage.put(InAppMessages.config(), 42)
    }
}
