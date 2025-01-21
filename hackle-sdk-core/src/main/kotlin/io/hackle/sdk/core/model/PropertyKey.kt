package io.hackle.sdk.core.model

data class PropertyKey(
    val type: Type,
    val name: String
) {
    enum class Type {
        COHORT,
        HACKLE,
        USER,
        EVENT
    }
}