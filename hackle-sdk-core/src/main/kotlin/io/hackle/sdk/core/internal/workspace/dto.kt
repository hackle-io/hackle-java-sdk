package io.hackle.sdk.core.internal.workspace

internal data class TargetDto(
    val conditions: List<ConditionDto>
) {
    data class ConditionDto(
        val key: KeyDto,
        val match: MatchDto
    )

    data class KeyDto(
        val type: String,
        val name: String
    )

    data class MatchDto(
        val type: String,
        val operator: String,
        val valueType: String,
        val values: List<Any>
    )

    data class NumberOfEventsInDaysDto(
        val eventKey: String,
        val days: Int
    )

    data class NumberOfEventsWithPropertyInDaysDto(
        val eventKey: String,
        val days: Int,
        val propertyFilter: ConditionDto
    )
}
