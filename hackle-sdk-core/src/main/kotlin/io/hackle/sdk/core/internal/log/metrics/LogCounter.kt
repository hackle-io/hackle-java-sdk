package io.hackle.sdk.core.internal.log.metrics

import io.hackle.sdk.core.internal.log.LogLevel
import io.hackle.sdk.core.internal.metrics.Counter
import io.hackle.sdk.core.internal.metrics.MetricRegistry

internal class LogCounter(registry: MetricRegistry) {

    private val counters: Map<LogLevel, Counter> = LogLevel.values().associateWith {
        Counter.builder("log.events").tag("level", it.name).register(registry)
    }

    fun increment(level: LogLevel) {
        counters[level]?.increment()
    }
}
