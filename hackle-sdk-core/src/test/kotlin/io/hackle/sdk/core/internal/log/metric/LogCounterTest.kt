package io.hackle.sdk.core.internal.log.metric

import io.hackle.sdk.core.internal.log.LogLevel
import io.hackle.sdk.core.internal.log.metrics.LogCounter
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class LogCounterTest {

    @Test
    fun `increment`() {
        val registry = CumulativeMetricRegistry()
        val logCounter = LogCounter(registry)

        repeat(1) { logCounter.increment(LogLevel.DEBUG) }
        repeat(2) { logCounter.increment(LogLevel.INFO) }
        repeat(3) { logCounter.increment(LogLevel.WARN) }
        repeat(4) { logCounter.increment(LogLevel.ERROR) }

        expectThat(registry.counter("log", "level" to "DEBUG").count()).isEqualTo(1)
        expectThat(registry.counter("log", "level" to "INFO").count()).isEqualTo(2)
        expectThat(registry.counter("log", "level" to "WARN").count()).isEqualTo(3)
        expectThat(registry.counter("log", "level" to "ERROR").count()).isEqualTo(4)
    }
}