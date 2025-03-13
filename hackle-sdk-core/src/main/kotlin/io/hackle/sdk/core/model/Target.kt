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
            EXISTS,
            NOT_EXISTS,
        }
    }

    /**
     * key가 TargetSegmentationExpression 인 경우
     */
    sealed class TargetSegmentationExpression {
        /**
         * TargetEvent 에 해당하는 TargetSegmentationExpression
         */
        sealed class NumberOfEventInDay: TargetSegmentationExpression() {
            abstract val eventKey: String
            abstract val days: Int

            /**
             * NUMBER_OF_EVENTS_IN_DAYS TargetSegmentationExpression
             */
            data class NumberOfEventsInDays(
                override val eventKey: String,
                override val days: Int
            ): NumberOfEventInDay()

            /**
             * NUMBER_OF_EVENTS_WITH_PROPERTY_IN_DAYS TargetSegmentationExpression
             */
            data class NumberOfEventsWithPropertyInDays(
                override val eventKey: String,
                override val days: Int,
                /**
                 * 이벤트 속성 필터
                 *
                 * EVENT_PROPERTY 타입만 허용됨
                 */
                val propertyFilter: Condition
            ): NumberOfEventInDay()
        }
    }
}
