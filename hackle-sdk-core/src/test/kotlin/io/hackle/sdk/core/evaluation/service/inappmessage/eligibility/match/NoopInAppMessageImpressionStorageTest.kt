package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.support.InAppMessages
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class NoopInAppMessageImpressionStorageTest {

    @Test
    fun `get should return an empty list`() {
        val result = NoopInAppMessageImpressionStorage.get(InAppMessages.config())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `set should not throw any exception`() {
        NoopInAppMessageImpressionStorage.set(InAppMessages.config(), impressions = listOf())
    }
}
