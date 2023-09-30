package io.hackle.sdk.internal.monitoring.metrics

internal data class MetricDto(
    val name: String,
    val tags: Map<String, String>,
    val type: String,
    val measurements: Map<String, Double>
)

internal data class MetricBatchDto(
    val metrics: List<MetricDto>
)
