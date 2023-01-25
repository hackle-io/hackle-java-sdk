package io.hackle.sdk.core.internal.metrics

import io.hackle.sdk.core.internal.metrics.Metric.Type.COUNTER

interface Counter : Metric {

    fun count(): Long

    fun increment(delta: Long)

    fun increment() = increment(1)

    override fun measure(): List<Measurement> {
        return listOf(Measurement(MetricField.COUNT, ::count))
    }

    private class Builder(name: String) : Metric.Builder<Counter>(name, COUNTER, MetricRegistry::counter)

    companion object {
        fun builder(name: String): Metric.Builder<Counter> {
            return Builder(name)
        }
    }
}
