package io.hackle.sdk.core.internal.metrics.logging

import io.hackle.sdk.core.internal.time.Clock
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.contains
import java.util.concurrent.TimeUnit

internal class LoggingMetricRegistryTest {

    @Test
    fun `logging`() {
        val logs = mutableListOf<String>()
        val registry = LoggingMetricRegistry.builder()
            .clock(Clock.SYSTEM)
            .flushIntervalMillis(1000)
            .logging { logs += it }
            .build()

        val counter = registry.counter("test.counter", "tag-1" to "tag-2")
        counter.increment(42)

        val timer = registry.timer("test.timer", "tag-3" to "tag-4")
        timer.record(320, TimeUnit.MILLISECONDS)

        registry.publish()

        expectThat(logs)
            .any { contains("test.counter {tag-1=tag-2} count=42.0") }
            .any { contains("test.timer {tag-3=tag-4} count=1.0 total=320.0 max=320.0 mean=320.0") }

        logs.clear()

        registry.publish()
        expectThat(logs)
            .any { contains("test.counter {tag-1=tag-2} count=0.0") }
            .any { contains("test.timer {tag-3=tag-4} count=0.0 total=0.0 max=0.0 mean=0.0") }
    }
}