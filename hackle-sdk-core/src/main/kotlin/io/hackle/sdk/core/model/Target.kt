package io.hackle.sdk.core.model

data class Target(
    val conditions: List<Condition>
) {

    data class Condition(
        val key: Key,
        val match: Match
    )

    data class Key(val type: Type, val name: String) {

        enum class Type {
            USER_ID,
            USER_PROPERTY,
            HACKLE_PROPERTY,
            SEGMENT,
            AB_TEST,
            FEATURE_FLAG,
            EVENT_PROPERTY,
            COHORT,
            NUMBER_OF_EVENTS_IN_DAYS,
            NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS
        }
    }

    data class Match(
        val type: Type,
        val operator: Operator,
        val valueType: ValueType,
        val values: List<Any>,
    ) {
        enum class Type {
            MATCH, NOT_MATCH
        }

        enum class Operator {
            IN,
            CONTAINS,
            STARTS_WITH,
            ENDS_WITH,
            GT,
            GTE,
            LT,
            LTE,
        }
    }

    sealed class TargetSegmentationExpression {
        data class NumberOfEventsInDays(
            val eventKey: String,
            val days: Int
        ): TargetSegmentationExpression()

        data class NumberOfEventsWithPropertyInDays(
            val eventKey: String,
            val days: Int,
            val propertyFilter: Condition
        ): TargetSegmentationExpression()
    }
}
