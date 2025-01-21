package io.hackle.sdk.core.model


class TargetSegmentationOption {
    data class TimeRange(
        val period: Int,
        val timeUnit: TimeUnit
    ) {
        enum class TimeUnit {
            DAYS,
            WEEKS,
        }
    }

    data class PropertyFilter(
        val propertyKey: PropertyKey,
        val match: Target.Match
    )
}
