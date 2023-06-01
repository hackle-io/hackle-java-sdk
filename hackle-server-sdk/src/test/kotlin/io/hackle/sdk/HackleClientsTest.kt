package io.hackle.sdk

import io.hackle.sdk.internal.client.HackleClientImpl
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isNotEqualTo
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors

internal class HackleClientsTest {

    @Test
    fun `create concurrency`() {
        val config = HackleConfig.builder()
            .sdkUrl("localhost")
            .eventUrl("localhost")
            .monitoringUrl("localhost")
            .build()

        val barrier = CyclicBarrier(100)
        val executor = Executors.newFixedThreadPool(100)

        val futures = List(100) {
            executor.submit(Callable {
                barrier.await()
                HackleClients.create("SDK_KEY", config)
            })
        }
        val clients = futures.map { it.get() }
        expectThat(clients.distinct()).hasSize(1)

        val client = HackleClients.create("SDK2", config)
        expectThat(clients[0]).isNotEqualTo(client)
    }

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