package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.MetricField
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.single

internal class DelegatingCounterTest {

    @Test
    fun `등록된 Counter 가 없으면 0`() {
        val registry = DelegatingMetricRegistry()
        val counter = registry.counter("counter")
        expectThat(counter)
            .isA<DelegatingCounter>()
            .get { count() }.isEqualTo(0)
    }

    @Test
    fun `등록된 Counter 로 위임한다`() {

        val cumulative1 = CumulativeMetricRegistry()
        val cumulative2 = CumulativeMetricRegistry()
        val registry = DelegatingMetricRegistry()
        registry.add(cumulative1)
        registry.add(cumulative2)

        registry.counter("counter").increment(42)

        expectThat(cumulative1.counter("counter").count()).isEqualTo(42)
        expectThat(cumulative2.counter("counter").count()).isEqualTo(42)
        expectThat(registry.counter("counter").count()).isEqualTo(42)
    }

    @Test
    fun `measure`() {
        val delgating = DelegatingMetricRegistry()
        val counter = delgating.counter("counter")

        val measurements = counter.measure()
        expectThat(measurements).single().and {
            get { field } isEqualTo MetricField.COUNT
            get { value } isEqualTo 0.0
        }

        counter.increment(42)
        expectThat(measurements).single().and {
            get { field } isEqualTo MetricField.COUNT
            get { value } isEqualTo 0.0
        }

        delgating.add(CumulativeMetricRegistry())
        counter.increment(42)
        expectThat(measurements).single().and {
            get { field } isEqualTo MetricField.COUNT
            get { value } isEqualTo 42.0
        }
    }
}