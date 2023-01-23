package io.hackle.sdk.core.internal.metrics.delegate

import io.hackle.sdk.core.internal.metrics.Metric
import io.hackle.sdk.core.internal.metrics.MetricRegistry

internal interface DelegatingMetric : Metric {

    fun add(registry: MetricRegistry)
}
