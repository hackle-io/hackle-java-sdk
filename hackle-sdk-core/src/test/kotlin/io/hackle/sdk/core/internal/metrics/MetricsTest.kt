package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import io.hackle.sdk.core.internal.metrics.delegate.DelegatingMetricRegistry
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.util.concurrent.TimeUnit

internal class MetricsTest {

    @Test
    fun `globalRegistry`() {
        expectThat(Metrics.globalRegistry).isA<DelegatingMetricRegistry>()
    }

    @Test
    fun `metric`() {
        val counter = Metrics.counter("counter")
        val timer = Metrics.timer("timer")

        counter.increment()
        timer.record(42, TimeUnit.MILLISECONDS)

        expectThat(Metrics.counter("counter").count()).isEqualTo(0)
        expectThat(Metrics.timer("timer").count()).isEqualTo(0)

        val cumulative = CumulativeMetricRegistry()
        Metrics.addRegistry(cumulative)

        counter.increment()
        timer.record(1, TimeUnit.MILLISECONDS)

        expectThat(Metrics.counter("counter").count()).isEqualTo(1)
        expectThat(Metrics.timer("timer").totalTime(TimeUnit.MILLISECONDS)).isEqualTo(1.0)

        Metrics.counter("counter", mapOf("tag" to "42")).increment(42)
        Metrics.timer("timer", mapOf("tag" to "42")).record(42, TimeUnit.MILLISECONDS)

        expectThat(Metrics.counter("counter").count()).isEqualTo(1)
        expectThat(Metrics.counter("counter", mapOf("tag" to "42")).count()).isEqualTo(42)

        expectThat(Metrics.timer("timer").count()).isEqualTo(1)
        expectThat(Metrics.timer("timer", mapOf("tag" to "42")).totalTime(TimeUnit.MILLISECONDS)).isEqualTo(42.0)
    }
}
