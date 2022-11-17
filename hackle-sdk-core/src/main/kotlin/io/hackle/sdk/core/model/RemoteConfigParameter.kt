package io.hackle.sdk.core.model

data class RemoteConfigParameter(
    val id: Long,
    val key: String,
    val type: ValueType,
    val identifierType: String,
    val targetRules: List<TargetRule>,
    val defaultValue: Value
) {

    data class Value(
        val id: Long,
        val rawValue: Any
    )

    data class TargetRule(
        val key: String,
        val name: String,
        val target: Target,
        val bucketId: Long,
        val value: Value
    )
}
