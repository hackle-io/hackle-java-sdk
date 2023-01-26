package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.AbstractMetric
import io.hackle.sdk.core.internal.metrics.Metric
import java.util.concurrent.atomic.AtomicReference

abstract class AbstractFlushMetric<M : Metric>(id: Metric.Id) : AbstractMetric(id), FlushMetric<M> {

    private val _current: AtomicReference<M> by lazy { AtomicReference(initialMetric()) }
    protected val current: M get() = _current.get()

    final override fun flush(): M {
        return _current.getAndSet(initialMetric())
    }

    abstract fun initialMetric(): M
}
