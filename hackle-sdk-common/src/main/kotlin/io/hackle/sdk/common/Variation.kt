package io.hackle.sdk.common

/**
 * @author Yong
 */
enum class Variation {

    A, B, C, D, E, F, G, H, I, J;

    val isControl: Boolean get() = this == CONTROL

    val isExperimental: Boolean get() = !isControl

    companion object {

        @JvmField
        val CONTROL = A

        private val VARIATIONS = values().flatMap { listOf(it.name to it, it.name.toLowerCase() to it) }.toMap()

        @JvmStatic
        fun from(key: String): Variation {
            return requireNotNull(VARIATIONS[key]) { "variation[$key]" }
        }

        @JvmStatic
        fun fromOrControl(key: String): Variation {
            return VARIATIONS[key] ?: CONTROL
        }
    }
}
