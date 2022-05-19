package io.hackle.sdk.common

/**
 * @author Yong
 */
data class User internal constructor(
    val identifiers: Identifiers,
    val properties: Map<String, Any>,
) {

    val id get() = identifiers[Identifiers.Type.ID]

    class Builder internal constructor() {

        private val properties = PropertiesBuilder()
        private val identifiers = Identifiers.builder()

        internal fun id(id: String) = apply { identifiers.add(Identifiers.Type.ID, id) }
        fun userId(userId: String?) = apply { identifiers.add(Identifiers.Type.USER, userId) }
        fun deviceId(deviceId: String?) = apply { identifiers.add(Identifiers.Type.DEVICE, deviceId) }
        fun identifier(type: String, value: String?) = apply { identifiers.add(type, value) }
        fun property(key: String, value: Int) = apply { properties.add(key, value) }
        fun property(key: String, value: Long) = apply { properties.add(key, value) }
        fun property(key: String, value: Double) = apply { properties.add(key, value) }
        fun property(key: String, value: Boolean) = apply { properties.add(key, value) }
        fun property(key: String, value: String?) = apply { value?.let { properties.add(key, it) } }

        fun build(): User {
            return User(
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
