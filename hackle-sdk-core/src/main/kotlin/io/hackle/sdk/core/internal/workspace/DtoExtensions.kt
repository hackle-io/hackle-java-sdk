package io.hackle.sdk.core.internal.workspace

import io.hackle.sdk.core.internal.utils.enumValueOfOrNull
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.TargetingType
import io.hackle.sdk.core.model.ValueType

internal fun TargetDto.ConditionDto.toConditionOrNull(targetingType: TargetingType): Target.Condition? {
    val key = key.toTargetKeyOrNull() ?: return null

    if (!targetingType.supports(key.type)) {
        return null
    }

    return Target.Condition(
        key = key,
        match = match.toMatchOrNull() ?: return null
    )
}

internal fun TargetDto.KeyDto.toTargetKeyOrNull(): Target.Key? {
    return Target.Key(
        type = parseEnumOrNull<Target.Key.Type>(type) ?: return null,
        name = name
    )
}

internal fun TargetDto.MatchDto.toMatchOrNull(): Target.Match? {
    return Target.Match(
        type = parseEnumOrNull<Target.Match.Type>(type) ?: return null,
        operator = parseEnumOrNull<Target.Match.Operator>(operator) ?: return null,
        valueType = parseEnumOrNull<ValueType>(valueType) ?: return null,
        values = values
    )
}

internal fun TargetDto.NumberOfEventsInDaysDto.toTargetSegmentationExpression(): Target.TargetSegmentationExpression.NumberOfEventInDay.NumberOfEventsInDays {
    return Target.TargetSegmentationExpression.NumberOfEventInDay.NumberOfEventsInDays(eventKey, days)
}

internal fun TargetDto.NumberOfEventsWithPropertyInDaysDto.toTargetSegmentationExpression(): Target.TargetSegmentationExpression.NumberOfEventInDay.NumberOfEventsWithPropertyInDays {
    val propertyFilter = propertyFilter.toConditionOrNull(TargetingType.PROPERTY) ?: throw IllegalArgumentException("propertyFilter is required")
    return Target.TargetSegmentationExpression.NumberOfEventInDay.NumberOfEventsWithPropertyInDays(eventKey, days, propertyFilter)
}

private inline fun <reified E : Enum<E>> parseEnumOrNull(name: String): E? {
    val enum = enumValueOfOrNull<E>(name) ?: return null
    return enum
}
