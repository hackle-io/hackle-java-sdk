package io.hackle.sdk.common

/**
 * @author Yong
 */
data class User internal constructor(
    val id: String,
    val properties: Map<String, Any>
) {

    class Builder(private val id: String) {
        private val properties = PropertiesBuilder()
        fun property(key: String, value: Int) = apply { properties.add(key, value) }
        fun property(key: String, value: Long) = apply { properties.add(key, value) }
        fun property(key: String, value: Double) = apply { properties.add(key, value) }
        fun property(key: String, value: Boolean) = apply { properties.add(key, value) }
        fun property(key: String, value: String?) = apply { value?.let { properties.add(key, it) } }
        fun build() = User(id, properties.build())
    }

    companion object {

        @JvmStatic
        fun of(id: String): User = User(id, emptyMap())

        @JvmStatic
        fun builder(id: String): Builder = Builder(id)
    }
}
