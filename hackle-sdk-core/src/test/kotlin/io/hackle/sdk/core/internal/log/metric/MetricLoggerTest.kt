package io.hackle.sdk.core.internal.log.metric

import io.hackle.sdk.core.internal.log.metrics.MetricLoggerFactory
import io.hackle.sdk.core.internal.metrics.cumulative.CumulativeMetricRegistry
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

internal class MetricLoggerTest {

    @Test
    fun `log count`() {
        val registry = CumulativeMetricRegistry()
        val factory = MetricLoggerFactory(registry)
        val logger = factory.getLogger("test")

        repeat(1) { logger.debug { "text" } }
        repeat(2) { logger.info { "text" } }
        repeat(3) { logger.warn { "text" } }
        repeat(3) { logger.error { "text" } }
        logger.error(IllegalArgumentException()) { "text" }

        expectThat(registry.counter("log.events", "level" to "debug").count()).isEqualTo(1)
        expectThat(registry.counter("log.events", "level" to "info").count()).isEqualTo(2)
        expectThat(registry.counter("log.events", "level" to "warn").count()).isEqualTo(3)
        expectThat(registry.counter("log.events", "level" to "error").count()).isEqualTo(4)
    }
}