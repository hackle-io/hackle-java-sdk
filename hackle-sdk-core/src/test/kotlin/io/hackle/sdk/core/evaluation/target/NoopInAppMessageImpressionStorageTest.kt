package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.model.InAppMessages
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NoopInAppMessageImpressionStorageTest {

    @Test
    fun `get should return an empty list`() {
        // Arrange
        val storage = NoopInAppMessageImpressionStorage
        val inAppMessage = InAppMessages.create()

        // Act
        val result = storage.get(inAppMessage)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `set should not throw any exception`() {
        // Arrange
        val storage = NoopInAppMessageImpressionStorage
        val inAppMessage = InAppMessages.create()

        // Act & Assert
        storage.set(inAppMessage, impressions = listOf())
    }
}
