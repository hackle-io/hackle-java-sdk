package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import io.hackle.sdk.core.internal.time.Clock
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.TimeUnit

internal class TimerTest {

    @Test
    fun `TimerSample`() {
        val clock = mockk<Clock> {
            every { tick() } returnsMany listOf(100, 142)
        }
        val sample = Timer.start(clock)
        val timer = CumulativeMetricRegistry().timer("timer")
        sample.stop(timer)

        expectThat(timer.totalTime(TimeUnit.NANOSECONDS)).isEqualTo(42.0)
    }

    @Test
    fun `builder`() {
        val timer = Timer.builder("test_timer")
            .tags(mapOf("a" to "1", "b" to "2"))
            .tags("c" to "3", "d" to "4")
            .tag("hello", "world")
            .register(CumulativeMetricRegistry())

        expectThat(timer.id) {
            get { name } isEqualTo "test_timer"
            get { tags } isEqualTo mapOf("a" to "1", "b" to "2", "c" to "3", "d" to "4", "hello" to "world")
            get { type } isEqualTo Metric.Type.TIMER
        }
    }
}