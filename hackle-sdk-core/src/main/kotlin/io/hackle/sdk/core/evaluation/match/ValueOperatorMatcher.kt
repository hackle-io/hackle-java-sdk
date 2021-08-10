package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target

internal class ValueOperatorMatcher {

    fun matches(userValue: Any, match: Target.Match): Boolean {
        val valueMatcher = ValueMatcher[match.valueType]
        val operatorMatcher = OperatorMatcher[match.operator]
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
