package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.Version


internal interface OperatorMatcher {
    fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean
}

internal object InMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.inMatch(userValue, it) }
    }
}

internal object ContainsMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.containsMatch(userValue, it) }
    }
}

internal object StartsWithMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.startsWithMatch(userValue, it) }
    }
}

internal object EndsWithMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.endsWithMatch(userValue, it) }
    }
}

internal object GreaterThanMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.greaterThanMatch(userValue, it) }
    }
}

internal object GreaterThanOrEqualToMatcher : OperatorMatcher {
   override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.greaterThanOrEqualToMatch(userValue, it) }
    }
}

internal object LessThanMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.lessThanMatch(userValue, it) }
    }
}

internal object LessThanOrEqualToMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return matchValue.any { valueMatcher.lessThanOrEqualToMatch(userValue, it) }
    }
}

internal object ExistsMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return valueMatcher.existsMatch(userValue)
    }
}


internal object NotExistsMatcher : OperatorMatcher {
    override fun matches(valueMatcher: ValueMatcher, userValue: Any?, matchValue: List<Any>): Boolean {
        return valueMatcher.notExistsMatch(userValue)
    }
}