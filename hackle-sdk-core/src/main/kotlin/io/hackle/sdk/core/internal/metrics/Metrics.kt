package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.internal.metrics.delegate.DelegatingMetricRegistry

object Metrics {

    private val log = Logger<Metrics>()

    val globalRegistry = DelegatingMetricRegistry()

    fun addRegistry(registry: MetricRegistry) {
        globalRegistry.add(registry)
        log.info { "MetricRegistry added [${registry.javaClass.simpleName}]" }
    }

    fun counter(name: String, tags: Map<String, String>): Counter {
        return globalRegistry.counter(name, tags)
    }

    fun counter(name: String, vararg tags: Pair<String, String>): Counter {
        return globalRegistry.counter(name, *tags)
    }

    fun timer(name: String, tags: Map<String, String>): Timer {
        return globalRegistry.timer(name, tags)
    }

    fun timer(name: String, vararg tags: Pair<String, String>): Timer {
        return globalRegistry.timer(name, *tags)
    }
}
