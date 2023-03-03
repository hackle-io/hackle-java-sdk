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
    val hackleProperties: Map<String, Any>,
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
            hackleProperties.add(user.hackleProperties)
        }

        private var id: String? = null
        private var userId: String? = null
        private var deviceId: String? = null
        private val identifiers = IdentifiersBuilder()
        private val properties = PropertiesBuilder()
        private val hackleProperties = PropertiesBuilder()

        fun id(id: String?) = apply { this.id = id }
        fun userId(userId: String?) = apply { this.userId = userId }
        fun deviceId(deviceId: String?) = apply { this.deviceId = deviceId }
        fun identifier(type: String, value: String?) = apply { identifiers.add(type, value) }
        fun property(key: String, value: Any?) = apply { properties.add(key, value) }
        fun hackleProperty(key: String, value: Any?) = apply { hackleProperties.add(key, value) }
        fun hackleProperties(hackleProperties: Map<String, Any>) = apply { this.hackleProperties.add(hackleProperties) }

        fun build(): User {
            return User(
                id = id,
                userId = userId,
                deviceId = deviceId,
                identifiers = identifiers.build(),
                properties = properties.build(),
                hackleProperties = hackleProperties.build()
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
