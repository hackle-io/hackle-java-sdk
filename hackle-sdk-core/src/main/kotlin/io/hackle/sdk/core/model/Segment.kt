package io.hackle.sdk.core.model

data class Segment(
    val id: Long,
    val key: String,
    val type: Type,
    val targets: List<Target>,
) {

    enum class Type {
        USER_ID, USER_PROPERTY
    }
}
