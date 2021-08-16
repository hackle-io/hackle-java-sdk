package io.hackle.sdk.core.model

sealed class Action {
    data class Variation(val variationId: Long) : Action()
    data class Bucket(val bucketId: Long) : Action()
}
