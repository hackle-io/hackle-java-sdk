package io.hackle.sdk.core.internal.metrics

enum class MetricField(val tagKey: String) {
    COUNT("count"),
    TOTAL("total"),
    MAX("max"),
    MEAN("mean")
}
