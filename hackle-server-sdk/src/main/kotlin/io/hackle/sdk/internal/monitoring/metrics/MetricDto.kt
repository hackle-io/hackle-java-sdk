package io.hackle.sdk.internal.monitoring.metrics

data class MetricDto(
    val name: String,
    val tags: Map<String, String>,
    val type: String,
    val measurements: Map<String, Double>
)

data class MetricBatchDto(
    val metrics: List<MetricDto>
)
