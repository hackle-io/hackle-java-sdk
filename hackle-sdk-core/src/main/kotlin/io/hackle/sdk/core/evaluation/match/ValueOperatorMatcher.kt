package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.ValueType

internal class ValueOperatorMatcher(
    private val factory: ValueOperatorMatcherFactory
) {

    fun matches(userValue: Any?, match: Target.Match): Boolean {
        val valueMatcher = factory.getValueMatcher(match.valueType)
        val operatorMatcher = factory.getOperatorMatcher(match.operator)

        @Suppress("UNCHECKED_CAST")
        val isMatched = when (userValue) {
            is Collection<*> -> arrayMatches(userValue as Collection<Any>, match, valueMatcher, operatorMatcher)
            else -> singleMatches(userValue, match, valueMatcher, operatorMatcher)
        }
        return match.type.matches(isMatched)
    }

    private fun singleMatches(
        userValue: Any?,
        match: Target.Match,
        valueMatcher: ValueMatcher,
        operatorMatcher: OperatorMatcher
    ): Boolean {
        return operatorMatcher.matches(valueMatcher, userValue, match.values)
    }

    private fun arrayMatches(
        userValues: Collection<Any>,
        match: Target.Match,
        valueMatcher: ValueMatcher,
        operatorMatcher: OperatorMatcher
    ): Boolean {
        return userValues.any { singleMatches(it, match, valueMatcher, operatorMatcher) }
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
            Target.Match.Operator.EXISTS -> ExistsMatcher
        }
    }
}
