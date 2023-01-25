package io.hackle.sdk.core.internal.log.metrics

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.MetricRegistry

class MetricLoggerFactory(registry: MetricRegistry) : Logger.Factory {
    private val counter = LogCounter(registry)
    override fun getLogger(name: String): Logger {
        return MetricLogger(counter)
    }
}
