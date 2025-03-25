package io.hackle.sdk.core.evaluation.match

internal interface OperatorMatcher {
    fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean
}

internal object InMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.inMatch(userValue, it) }
    }
}

internal object ContainsMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.containsMatch(userValue, it) }
    }
}

internal object StartsWithMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.startsWithMatch(userValue, it) }
    }
}

internal object EndsWithMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.endsWithMatch(userValue, it) }
    }
}

internal object GreaterThanMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.greaterThanMatch(userValue, it) }
    }
}

internal object GreaterThanOrEqualToMatcher : OperatorMatcher {
   override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
       if(userValue == null) return false
        return matchValues.any { valueMatcher.greaterThanOrEqualToMatch(userValue, it) }
    }
}

internal object LessThanMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.lessThanMatch(userValue, it) }
    }
}

internal object LessThanOrEqualToMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        if(userValue == null) return false
        return matchValues.any { valueMatcher.lessThanOrEqualToMatch(userValue, it) }
    }
}

internal object ExistsMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValues: List<Any>): Boolean {
        return userValue != null
    }
}
