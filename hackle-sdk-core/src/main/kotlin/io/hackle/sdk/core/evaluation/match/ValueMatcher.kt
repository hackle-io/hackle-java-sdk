package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Match.ValueType.*


internal fun interface ValueMatcher {
    fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean

    companion object {
        operator fun get(valueType: Target.Match.ValueType): ValueMatcher {
            return when (valueType) {
                STRING -> StringMatcher
                NUMBER -> NumberMatcher
                BOOLEAN -> BooleanMatcher
            }
        }

        inline fun <reified T> matches(userValue: Any, matchValue: Any, operatorMatches: (T, T) -> Boolean): Boolean {
            return if (userValue is T && matchValue is T) {
                operatorMatches(userValue, matchValue)
            } else {
                false
            }
        }
    }
}

internal object StringMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return ValueMatcher.matches<String>(userValue, matchValue, operatorMatcher::matches)
    }
}

internal object NumberMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return ValueMatcher.matches<Number>(userValue, matchValue, operatorMatcher::matches)
    }
}

internal object BooleanMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return ValueMatcher.matches<Boolean>(userValue, matchValue, operatorMatcher::matches)
    }
}
