package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.ValueType

internal class ValueOperatorMatcher(
    private val factory: ValueOperatorMatcherFactory
) {

    fun matches(userValue: Any, match: Target.Match): Boolean {
        val valueMatcher = factory.getValueMatcher(match.valueType)
        val operatorMatcher = factory.getOperatorMatcher(match.operator)
        val isMatched = match.values.any { valueMatcher.matches(operatorMatcher, userValue, it) }
        return match.type.matches(isMatched)
    }
}

internal class ValueOperatorMatcherFactory {

    fun getValueMatcher(valueType: ValueType): ValueMatcher {
        return when (valueType) {
            ValueType.STRING, ValueType.JSON -> StringMatcher
            ValueType.NUMBER -> NumberMatcher
            ValueType.BOOLEAN -> BooleanMatcher
            ValueType.VERSION -> VersionMatcher
        }
    }

    fun getOperatorMatcher(operator: Target.Match.Operator): OperatorMatcher {
        return when (operator) {
            Target.Match.Operator.IN -> InMatcher
            Target.Match.Operator.CONTAINS -> ContainsMatcher
            Target.Match.Operator.STARTS_WITH -> StartsWithMatcher
            Target.Match.Operator.ENDS_WITH -> EndsWithMatcher
            Target.Match.Operator.GT -> GreaterThanMatcher
            Target.Match.Operator.GTE -> GreaterThanOrEqualToMatcher
            Target.Match.Operator.LT -> LessThanMatcher
            Target.Match.Operator.LTE -> LessThanOrEqualToMatcher
        }
    }
}
