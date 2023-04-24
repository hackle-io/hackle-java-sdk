package io.hackle.sdk

import io.hackle.sdk.internal.client.HackleClientImpl
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA

internal class HackleClientsTest {

    @Test
    fun `create`() {

        val config = HackleConfig.builder()
            .sdkUrl("localhost")
            .eventUrl("localhost")
            .monitoringUrl("localhost")
            .build()

        val client = HackleClients.create("SDK_KEY", config)

        expectThat(client).isA<HackleClientImpl>()
    }
}