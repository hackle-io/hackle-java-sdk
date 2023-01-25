package io.hackle.sdk.core.internal.metrics

class Measurement(
    val field: MetricField,
    private val valueSupplier: () -> Number
) {
    val value: Double get() = valueSupplier().toDouble()
}
