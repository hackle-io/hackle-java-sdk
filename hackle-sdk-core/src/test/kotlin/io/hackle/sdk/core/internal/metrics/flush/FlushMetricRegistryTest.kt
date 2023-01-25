package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class FlushMetricRegistryTest {

    private val flush = AtomicInteger()

    private val sut = object : FlushMetricRegistry(Clock.SYSTEM, mockk(), 100) {
        override fun flushMetric(metrics: List<Metric>) {
            flush.incrementAndGet()
        }
    }

    @Test
    fun `FlushCounter`() {
        val counter = sut.createCounter(Metric.Id("counter", emptyMap(), Metric.Type.COUNTER))
        expectThat(counter).isA<FlushCounter>()
    }

    @Test
    fun `FlushTimer`() {
        val timer = sut.createTimer(Metric.Id("timer", emptyMap(), Metric.Type.TIMER))
        expectThat(timer).isA<FlushTimer>()
    }

    @Test
    fun `reset metric after publish`() {
        val counter = sut.counter("counter")
        val timer = sut.timer("timer")

        counter.increment()
        timer.record(100, TimeUnit.SECONDS)

        expectThat(counter.count()).isEqualTo(1)
        expectThat(timer.count()).isEqualTo(1)

        sut.publish()

        expectThat(counter.count()).isEqualTo(0)
        expectThat(timer.count()).isEqualTo(0)

        expectThat(flush.get()).isEqualTo(1)
    }
}