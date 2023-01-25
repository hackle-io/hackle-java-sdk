package io.hackle.sdk.core.internal.metrics.cumulative

import io.hackle.sdk.core.internal.metrics.Metric
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isSameInstanceAs

internal class CumulativeMetricRegistryTest {

    @Test
    fun `counter`() {
        val id = Metric.Id("name", emptyMap(), Metric.Type.COUNTER)
        val registry = CumulativeMetricRegistry()
        val counter = registry.createCounter(id)
        expectThat(counter)
            .isA<CumulativeCounter>()
            .get { this.id }.isSameInstanceAs(id)
    }

    @Test
    fun `timer`() {
        val id = Metric.Id("name", emptyMap(), Metric.Type.TIMER)
        val registry = CumulativeMetricRegistry()
        val timer = registry.createTimer(id)
        expectThat(timer)
            .isA<CumulativeTimer>()
            .get { this.id }.isSameInstanceAs(id)
    }
}