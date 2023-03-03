package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.ValueConverter.asBooleanOrNull
import io.hackle.sdk.core.model.ValueConverter.asDoubleOrNull
import io.hackle.sdk.core.model.ValueConverter.asStringOrNull
import io.hackle.sdk.core.model.ValueConverter.asVersionOrNull


internal fun interface ValueMatcher {
    fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean
}

internal object StringMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return operatorMatcher.matches(
            userValue = asStringOrNull(userValue) ?: return false,
            matchValue = asStringOrNull(matchValue) ?: return false
        )
    }
}

internal object NumberMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return operatorMatcher.matches(
            userValue = asDoubleOrNull(userValue) ?: return false,
            matchValue = asDoubleOrNull(matchValue) ?: return false
        )
    }
}

internal object BooleanMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return operatorMatcher.matches(
            userValue = asBooleanOrNull(userValue) ?: return false,
            matchValue = asBooleanOrNull(matchValue) ?: return false
        )
    }
}

internal object VersionMatcher : ValueMatcher {
    override fun matches(operatorMatcher: OperatorMatcher, userValue: Any, matchValue: Any): Boolean {
        return operatorMatcher.matches(
            userValue = asVersionOrNull(userValue) ?: return false,
            matchValue = asVersionOrNull(matchValue) ?: return false
        )
    }
}
