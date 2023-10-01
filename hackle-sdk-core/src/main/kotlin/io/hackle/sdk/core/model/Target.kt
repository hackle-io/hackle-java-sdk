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
}
