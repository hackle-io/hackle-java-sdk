package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class CounterTest {

    @Test
    fun `builder`() {
        val counter = Counter.builder("test_counter")
            .tags(mapOf("a" to "1", "b" to "2"))
            .tags("c" to "3", "d" to "4")
            .tag("hello", "world")
            .register(CumulativeMetricRegistry())

        expectThat(counter.id) {
            get { name } isEqualTo "test_counter"
            get { tags } isEqualTo mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "hello" to "world")
            get { type } isEqualTo Metric.Type.COUNTER
        }
    }
}