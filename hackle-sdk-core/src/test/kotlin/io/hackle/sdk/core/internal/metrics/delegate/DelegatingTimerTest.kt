package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.MetricField
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.withElementAt
import java.util.concurrent.TimeUnit

internal class DelegatingTimerTest {

    @Test
    fun `등록된 timer 가 없으면 0`() {
        val delegating = DelegatingMetricRegistry()
        val timer = delegating.timer("timer")
        expectThat(timer)
            .isA<DelegatingTimer>().and {
                get { count() } isEqualTo 0
                get { totalTime(TimeUnit.NANOSECONDS) } isEqualTo 0.0
                get { max(TimeUnit.NANOSECONDS) } isEqualTo 0.0
                get { mean(TimeUnit.NANOSECONDS) } isEqualTo 0.0
            }
    }

    @Test
    fun `등록된 Timer 로 위임`() {
        val delegating = DelegatingMetricRegistry()
        val cumulative1 = CumulativeMetricRegistry()
        val cumulative2 = CumulativeMetricRegistry()
        delegating.add(cumulative1)
        delegating.add(cumulative2)

        val timer = delegating.timer("timer")
        timer.record(42, TimeUnit.NANOSECONDS)

        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
        expectThat(delegating.timer("timer").totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
        expectThat(cumulative1.timer("timer").totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
        expectThat(cumulative2.timer("timer").totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
    }

    @Test
    fun `lambda record`() {
        val clock = mockk<Clock> {
            every { tick() } returnsMany listOf(100, 142)
        }
        val registry = DelegatingMetricRegistry(clock)
        registry.add(CumulativeMetricRegistry())
        val timer = registry.timer("timer")
        val actual = timer.record { "42" }
        expectThat(actual).isEqualTo("42")
        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
    }

    @Test
    fun `measure`() {
        val delegating = DelegatingMetricRegistry()
        val timer = delegating.timer("timer")

        val measurements = timer.measure()
        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo MetricField.COUNT
                get { value } isEqualTo 0.0
            }
            .withElementAt(1) {
                get { field } isEqualTo MetricField.TOTAL
                get { value } isEqualTo 0.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MetricField.MAX
                get { value } isEqualTo 0.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MetricField.MEAN
                get { value } isEqualTo 0.0
            }

        timer.record(42, TimeUnit.MILLISECONDS)
        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo MetricField.COUNT
                get { value } isEqualTo 0.0
            }
            .withElementAt(1) {
                get { field } isEqualTo MetricField.TOTAL
                get { value } isEqualTo 0.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MetricField.MAX
                get { value } isEqualTo 0.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MetricField.MEAN
                get { value } isEqualTo 0.0
            }

        delegating.add(CumulativeMetricRegistry())
        timer.record(42, TimeUnit.MILLISECONDS)
        expectThat(measurements).hasSize(4)
            .withElementAt(0) {
                get { field } isEqualTo MetricField.COUNT
                get { value } isEqualTo 1.0
            }
            .withElementAt(1) {
                get { field } isEqualTo MetricField.TOTAL
                get { value } isEqualTo 42.0
            }
            .withElementAt(2) {
                get { field } isEqualTo MetricField.MAX
                get { value } isEqualTo 42.0
            }
            .withElementAt(3) {
                get { field } isEqualTo MetricField.MEAN
                get { value } isEqualTo 42.0
            }
    }
}