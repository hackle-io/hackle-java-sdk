package io.hackle.sdk.core.model

interface RemoteConfigParameter : Entity {
    override val id: Long
    val key: String
    val type: ValueType

    data class Value(
        val id: Long,
        val rawValue: Any,
    )

    data class TargetRule(
        val key: String,
        val name: String,
        val target: Target,
        val bucketId: Long,
        val value: Value,
    )

    companion object {
        fun <T : Any> cast(type: ValueType, value: Any): T? {
            @Suppress("UNCHECKED_CAST")
            return when (type) {
                ValueType.STRING -> value as? String
                ValueType.NUMBER -> value as? Number
                ValueType.BOOLEAN -> value as? Boolean
                ValueType.VERSION, ValueType.JSON -> null
            } as? T
        }
    }
}

abstract class AbstractRemoteConfigParameter : AbstractEntity(), RemoteConfigParameter {
    final override val serviceType: ServiceType get() = ServiceType.REMOTE_CONFIG

    override fun toString(): String {
        return "RemoteConfigParameterConfig(id=$id, key=$key)"
    }
}

fun <T : Any> ValueType.cast(value: RemoteConfigParameter.Value): T? {
    return RemoteConfigParameter.cast(this, value)
}
