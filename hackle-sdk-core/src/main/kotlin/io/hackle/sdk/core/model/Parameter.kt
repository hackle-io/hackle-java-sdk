package io.hackle.sdk.core.model

data class Parameter(
    val key: String,
    val value: Any,
    val type: Type
) {

    enum class Type {
        STRING,
        NUMBER,
        BOOLEAN,
        JSON
    }
}
