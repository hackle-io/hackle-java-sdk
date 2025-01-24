package io.hackle.sdk.core.model

sealed class TargetSegmentationExpression {
    abstract val type: Target.Key.Type

    data class NumberOfEventsInDays(
        val eventKey: String,
        val timeRange: TargetSegmentationOption.TimeRange,
        val filters: List<TargetSegmentationOption.PropertyFilter>?
    ): TargetSegmentationExpression() {
        companion object {
            const val MAX_DAY_PERIOD: Int = 30
        }

        override val type: Target.Key.Type
            get() = Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS

        init {
            //require(timeRange.timeUnit == TargetSegmentationOption.TimeRange.TimeUnit.DAYS) {
            //    "invalid time range time unit type, only supported ${TargetSegmentationOption.TimeRange.TimeUnit.DAYS} time unit type"
            //}
            require(timeRange.periodDays <= MAX_DAY_PERIOD) {
                "period max value 30, input value : ${timeRange.periodDays}"
            }
        }
    }
}
