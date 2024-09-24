package io.hackle.sdk.core.model

import io.hackle.sdk.common.HacklePushSubscriptionOperations
import io.hackle.sdk.common.HacklePushSubscriptionStatus
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class HacklePushSubscriptionOperationsExtensionsTest {

    @Test
    fun `toEvent`() {

        val operations = HacklePushSubscriptionOperations.builder()
            .global(HacklePushSubscriptionStatus.UNSUBSCRIBED)
            .build()

        val event = operations.toEvent()

        expectThat(event) {
            get { key } isEqualTo "\$push_subscriptions"
            get { properties } isEqualTo mapOf(
                "\$global" to "UNSUBSCRIBED"
            )
        }
    }
}
