package io.hackle.sdk.core.internal.metrics

abstract class AbstractMetric(final override val id: Metric.Id) : Metric {

    final override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is Metric -> false
            else -> this.id == other.id
        }
    }

    final override fun hashCode(): Int {
        return id.hashCode()
    }
}
