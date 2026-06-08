package io.hackle.sdk.common

import java.util.Locale.getDefault

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

        private val VARIATIONS =
            Variation.entries.flatMap { listOf(it.name to it, it.name.lowercase(getDefault()) to it) }.toMap()

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
