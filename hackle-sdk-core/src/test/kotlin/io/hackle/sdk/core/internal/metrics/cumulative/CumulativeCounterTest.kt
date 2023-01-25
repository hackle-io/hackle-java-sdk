package io.hackle.sdk.core.internal.metrics.cumulative

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.single
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class CumulativeCounterTest {

    @Test
    fun `concurrency increment`() {
        val counter = CumulativeMetricRegistry().counter("counter")

        val executor = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(10000)
        repeat(10000) {
            executor.execute {
                counter.increment()
                latch.countDown()
            }
        }

        latch.await()
        expectThat(counter.count()).isEqualTo(10000)
    }

    @Test
    fun `measure`() {
        val counter = CumulativeMetricRegistry().counter("counter")
        val measurements = counter.measure()

        expectThat(measurements).single().get { value }.isEqualTo(0.0)
        counter.increment(42)
        expectThat(measurements).single().get { value }.isEqualTo(42.0)
    }
}