package io.hackle.sdk.core.internal.metrics.flush

import io.hackle.sdk.core.internal.metrics.Metric

interface FlushMetric<out M : Metric> : Metric {

    fun flush(): M
}
