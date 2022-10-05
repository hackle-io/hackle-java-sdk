package io.hackle.sdk.core.model

data class Variation(
    val id: Long,
    val key: String,
    val isDropped: Boolean,
    val parameterConfigurationId: Long?,
)
