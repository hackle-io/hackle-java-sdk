package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.model.InAppMessage
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NoopInAppMessageImpressionStorageTest {

    @Test
    fun `get should return an empty list`() {
        // Arrange
        val storage = NoopInAppMessageImpressionStorage
        val inAppMessage = InAppMessage(
            id = 1L,
            key = 1L,
            status = InAppMessage.Status.ACTIVE,
            period = InAppMessage.Period.Always,
            eventTrigger = InAppMessage.EventTrigger(emptyList(), null),
            targetContext = InAppMessage.TargetContext(emptyList(), emptyList()),
            messageContext = InAppMessage.MessageContext(
                defaultLang = "en",
                experimentContext = null,
                platformTypes = emptyList(),
                orientations = emptyList(),
                messages = emptyList()
            )
        )

        // Act
        val result = storage.get(inAppMessage)

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `set should not throw any exception`() {
        // Arrange
        val storage = NoopInAppMessageImpressionStorage
        val inAppMessage = InAppMessage(
            id = 1L,
            key = 1L,
            status = InAppMessage.Status.ACTIVE,
            period = InAppMessage.Period.Always,
            eventTrigger = InAppMessage.EventTrigger(emptyList(), null),
            targetContext = InAppMessage.TargetContext(emptyList(), emptyList()),
            messageContext = InAppMessage.MessageContext(
                defaultLang = "en",
                experimentContext = null,
                platformTypes = emptyList(),
                orientations = emptyList(),
                messages = emptyList()
            )
        )

        // Act & Assert
        storage.set(inAppMessage, impressions = listOf())
    }
}