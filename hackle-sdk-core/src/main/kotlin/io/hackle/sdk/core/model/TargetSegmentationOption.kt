package io.hackle.sdk.core.model


class TargetSegmentationOption {
    data class TimeRange(
        val period: Int,
        val timeUnit: TimeUnit
    ) {
        val periodDays: Int
            get() {
                return if(timeUnit == TimeUnit.DAYS) {
                    period
                } else {
                    period * WEEKDAY
                }
            }

        enum class TimeUnit {
            DAYS,
            WEEKS,
        }

        companion object {
            private const val WEEKDAY = 7
        }
    }

    data class PropertyFilter(
        val propertyKey: PropertyKey,
        val match: Target.Match
    )
}
