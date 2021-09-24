package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Version


internal fun interface ValueMatcher {
    fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean

    companion object {
        inline fun <reified T> matches(userValue: Any, matchValue: Any, operatorMatches: (T, T) -> Boolean): Boolean {
            return matches(userValue, matchValue, { it as? T }, operatorMatches)
        }

        inline fun <reified T> matches(
            userValue: Any,
            matchValue: Any,
            transform: (Any) -> T?,
            operatorMatches: (T, T) -> Boolean,
        ): Boolean {
            val typedUserValue: T = transform(userValue) ?: return false
            val typedMatchValue: T = transform(matchValue) ?: return false
            return operatorMatches(typedUserValue, typedMatchValue)
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

internal object VersionMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return ValueMatcher.matches(userValue, matchValue, Version::parseOrNull, operatorMatcher::matches)
    }
}
