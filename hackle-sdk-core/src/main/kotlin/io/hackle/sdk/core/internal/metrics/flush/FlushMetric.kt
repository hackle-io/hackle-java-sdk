package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Metric

internal interface FlushMetric<out M : Metric> : Metric {

    fun flush(): M
}
