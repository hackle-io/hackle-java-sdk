package io.hackle.sdk.core.internal.metrics.noop

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.Metric.Id
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.util.concurrent.TimeUnit

internal class NoopTimerTest {
    @Test
    fun `always zero`() {
        val timer = NoopTimer(Id("timer", emptyMap(), Metric.Type.TIMER))
        expectThat(timer) {
            get { count() } isEqualTo 0
            get { totalTime(TimeUnit.NANOSECONDS) } isEqualTo 0.0
            get { max(TimeUnit.NANOSECONDS) } isEqualTo 0.0
            get { mean(TimeUnit.NANOSECONDS) } isEqualTo 0.0
        }
        val measurements = timer.measure()
        expectThat(measurements).hasSize(4).all { get { value } isEqualTo 0.0 }

        timer.record(42, TimeUnit.NANOSECONDS)
        timer.record { Thread.sleep(42) }
        expectThat(timer) {
            get { count() } isEqualTo 0
            get { totalTime(TimeUnit.NANOSECONDS) } isEqualTo 0.0
            get { max(TimeUnit.NANOSECONDS) } isEqualTo 0.0
            get { mean(TimeUnit.NANOSECONDS) } isEqualTo 0.0
        }
        expectThat(measurements).hasSize(4).all { get { value } isEqualTo 0.0 }
    }
}