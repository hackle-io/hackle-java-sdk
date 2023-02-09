package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricField
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeCounter
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.single
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch

internal class FlushCounterTest {

    @Test
    fun `increment only`() {
        val counter = counter()
        repeat(100) {
            counter.increment()
            expectThat(counter.count()).isEqualTo((it + 1).toLong())
        }
    }

    @Test
    fun `increment with flush`() {
        val counter = counter()
        repeat(100) {
            counter.increment()
            expectThat(counter.count()).isEqualTo((it + 1).toLong())
        }
        counter.flush() // flush
        expectThat(counter.count()).isEqualTo(0)
        repeat(100) {
            counter.increment()
            expectThat(counter.count()).isEqualTo((it + 1).toLong())
        }
    }

    @Test
    fun `concurrency increment`() {
        val counter = counter()

        val latch = CountDownLatch(10000)
        val jobs = List(10000) {
            CompletableFuture.supplyAsync {
                if (it % 2 == 0) {
                    counter.apply { increment() }
                } else {
                    counter.flush()
                }.also {
                    latch.countDown()
                }
            }
        }

        latch.await()
        val count = jobs.asSequence()
            .map { it.join() }
            .filterIsInstance<CumulativeCounter>()
            .sumOf { it.count() } + counter.flush().count()

        expectThat(count).isEqualTo(5000)
    }

    @Test
    fun `measure`() {
        val counter = counter()
        counter.increment(42)

        val measurements = counter.measure()
        expectThat(measurements).single().and {
            get { field } isEqualTo MetricField.COUNT
            get { value } isEqualTo 42.0
        }

        counter.flush()
        expectThat(measurements).single().and {
            get { field } isEqualTo MetricField.COUNT
            get { value } isEqualTo 0.0
        }
    }

    private fun counter(): FlushCounter {
        return FlushCounter(Metric.Id("counter", emptyMap(), Metric.Type.COUNTER))
    }
}