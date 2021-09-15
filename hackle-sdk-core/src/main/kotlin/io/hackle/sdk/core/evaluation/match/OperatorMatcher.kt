package io.hackle.sdk.core.evaluation.match


internal interface OperatorMatcher {
    fun matches(userValue: String, matchValue: String): Boolean
    fun matches(userValue: Number, matchValue: Number): Boolean
    fun matches(userValue: Boolean, matchValue: Boolean): Boolean
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

internal object LessThanOrEqualToMatcher : OperatorMatcher {
    override fun matches(userValue: String, matchValue: String): Boolean = false
    override fun matches(userValue: Number, matchValue: Number): Boolean = userValue.toDouble() <= matchValue.toDouble()
    override fun matches(userValue: Boolean, matchValue: Boolean): Boolean = false
}
