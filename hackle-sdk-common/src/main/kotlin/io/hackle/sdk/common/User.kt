package io.hackle.sdk.common

/**
 * @author Yong
 */
data class User internal constructor(
    val id: String?,
    val userId: String?,
    val deviceId: String?,
    val identifiers: Map<String, String>,
    val properties: Map<String, Any>,
) {

    fun toBuilder(): Builder {
        return Builder(this)
    }

    class Builder internal constructor() {

        internal constructor(user: User) : this() {
            id(user.id)
            userId(user.userId)
            deviceId(user.deviceId)
            identifiers.add(user.identifiers)
            properties.add(user.properties)
        }

        private var id: String? = null
        private var userId: String? = null
        private var deviceId: String? = null
        private val identifiers = IdentifiersBuilder()
        private val properties = PropertiesBuilder()

        fun id(id: String?) = apply { this.id = id }
        fun userId(userId: String?) = apply { this.userId = userId }
        fun deviceId(deviceId: String?) = apply { this.deviceId = deviceId }
        fun identifier(type: String, value: String?) = apply { identifiers.add(type, value) }
        fun identifiers(identifiers: Map<String, String?>?) = apply { identifiers?.let { this.identifiers.add(it) } }
        fun property(key: String, value: Any?) = apply { properties.add(key, value) }
        fun properties(properties: Map<String, Any?>?) = apply { properties?.let { this.properties.add(it) } }

        fun build(): User {
            return User(
                id = id,
                userId = userId,
                deviceId = deviceId,
                identifiers = identifiers.build(),
                properties = properties.build()
            )
        }
    }

    companion object {

        @JvmStatic
        fun of(id: String): User {
            return Builder().id(id).build()
        }

        @JvmStatic
        fun builder(id: String): Builder {
            return Builder().id(id)
        }

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }
}
