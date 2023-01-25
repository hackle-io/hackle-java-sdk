package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.time.Clock
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class MetricRegistry(protected val clock: Clock) : Closeable {

    private val lock = Any()
    protected fun <T> lock(block: () -> T): T = synchronized(lock) { block() }

    // Use ConcurrentHashMap to avoid ConcurrentModificationException.
    private val _metrics = ConcurrentHashMap<Metric.Id, Metric>()
    val metrics: List<Metric> get() = Collections.unmodifiableList(ArrayList(_metrics.values))

    abstract fun createCounter(id: Metric.Id): Counter

    abstract fun createTimer(id: Metric.Id): Timer

    fun counter(name: String, tags: Map<String, String>): Counter {
        return Counter.builder(name).tags(tags).register(this)
    }

    fun counter(name: String, vararg tags: Pair<String, String>): Counter {
        return Counter.builder(name).tags(*tags).register(this)
    }

    fun timer(name: String, tags: Map<String, String>): Timer {
        return Timer.builder(name).tags(tags).register(this)
    }

    fun timer(name: String, vararg tags: Pair<String, String>): Timer {
        return Timer.builder(name).tags(*tags).register(this)
    }

    internal fun counter(id: Metric.Id): Counter {
        return registerMetricIfNecessary(id, this::createCounter)
    }

    internal fun timer(id: Metric.Id): Timer {
        return registerMetricIfNecessary(id, this::createTimer)
    }

    private inline fun <reified M : Metric> registerMetricIfNecessary(
        id: Metric.Id,
        noinline create: (Metric.Id) -> M
    ): M {
        val metric = lock {
            // Do NOT use computeIfAbsent for support below Android 24
            _metrics.getOrPut(id) { create(id) }
        }
        return requireNotNull(metric as? M) {
            "Metric already registered with different type [${id.name}, ${metric::class.java.simpleName}/${M::class.java.simpleName}]"
        }
    }

    override fun close() {}
}
