package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target

internal class ValueOperatorMatcher(
    private val factory: ValueOperatorMatcherFactory
) {

    fun matches(userValue: Any, match: Target.Match): Boolean {
        val valueMatcher = factory.getValueMatcher(match.valueType)
        val operatorMatcher = factory.getOperatorMatcher(match.operator)
        val isMatched = match.values.any { valueMatcher.matches(operatorMatcher, userValue, it) }
        return match.type.matches(isMatched)
    }

    private fun Target.Match.Type.matches(isMatched: Boolean): Boolean {
        return when (this) {
            Target.Match.Type.MATCH -> isMatched
            Target.Match.Type.NOT_MATCH -> !isMatched
        }
    }
}

internal class ValueOperatorMatcherFactory {

    fun getValueMatcher(valueType: Target.Match.ValueType): ValueMatcher {
        return when (valueType) {
            Target.Match.ValueType.STRING -> StringMatcher
            Target.Match.ValueType.NUMBER -> NumberMatcher
            Target.Match.ValueType.BOOLEAN -> BooleanMatcher
            Target.Match.ValueType.VERSION -> VersionMatcher
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
