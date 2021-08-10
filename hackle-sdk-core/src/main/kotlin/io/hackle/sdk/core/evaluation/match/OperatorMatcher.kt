package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Target.Match.Operator
import io.hackle.sdk.core.model.Target.Match.Operator.*


internal interface OperatorMatcher {

    fun matches(userValue: String, matchValue: String): Boolean
    fun matches(userValue: Number, matchValue: Number): Boolean
    fun matches(userValue: Boolean, matchValue: Boolean): Boolean

    companion object {
        operator fun get(operator: Operator): OperatorMatcher {
            return when (operator) {
                IN -> InMatcher
                CONTAINS -> ContainsMatcher
                STARTS_WITH -> StartsWithMatcher
                ENDS_WITH -> EndsWithMatcher
                GT -> GreaterThanMatcher
                GTE -> GreaterThanOrEqualToMatcher
                LT -> LessThanMatcher
                LTE -> LessThanOrEqualsToMatcher
            }
        }
    }
}

internal object InMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = userValue == matchValue
    override fun matches(userValue: Number, matchValue: Number): Boolean = userValue.toDouble() == matchValue.toDouble()
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = userValue == matchValue
}

internal object ContainsMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = userValue.contains(matchValue)
    override fun matches(userValue: Number, matchValue: Number): Boolean = false
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}

internal object StartsWithMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = userValue.startsWith(matchValue)
    override fun matches(userValue: Number, matchValue: Number): Boolean = false
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}

internal object EndsWithMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = userValue.endsWith(matchValue)
    override fun matches(userValue: Number, matchValue: Number): Boolean = false
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}

internal object GreaterThanMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = false
    override fun matches(userValue: Number, matchValue: Number): Boolean = userValue.toDouble() > matchValue.toDouble()
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}

internal object GreaterThanOrEqualToMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = false
    override fun matches(userValue: Number, matchValue: Number): Boolean = userValue.toDouble() >= matchValue.toDouble()
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}

internal object LessThanMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = false
    override fun matches(userValue: Number, matchValue: Number): Boolean = userValue.toDouble() < matchValue.toDouble()
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}

internal object LessThanOrEqualsToMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = false
    override fun matches(userValue: Number, matchValue: Number): Boolean = userValue.toDouble() <= matchValue.toDouble()
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}
