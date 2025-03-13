package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.model.ValueConverter.asBooleanOrNull
import io.hackle.sdk.core.model.ValueConverter.asDoubleOrNull
import io.hackle.sdk.core.model.ValueConverter.asStringOrNull
import io.hackle.sdk.core.model.ValueConverter.asVersionOrNull


internal interface ValueMatcher {
    fun inMatch(userValue: Any?, matchValue: Any): Boolean
    fun containsMatch(userValue: Any?, matchValue: Any): Boolean
    fun startsWithMatch(userValue: Any?, matchValue: Any): Boolean
    fun endsWithMatch(userValue: Any?, matchValue: Any): Boolean
    fun greaterThanMatch(userValue: Any?, matchValue: Any): Boolean
    fun greaterThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean
    fun lessThanMatch(userValue: Any?, matchValue: Any): Boolean
    fun lessThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean
    fun existsMatch(userValue: Any?): Boolean
    fun notExistsMatch(userValue: Any?): Boolean
}

internal object StringMatcher : ValueMatcher {
    override fun inMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString == matchString
    }

    override fun containsMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString.contains(matchString)
    }

    override fun startsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString.startsWith(matchString)
    }

    override fun endsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString.endsWith(matchString)
    }

    override fun greaterThanMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString > matchString
    }

    override fun greaterThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString >= matchString
    }

    override fun lessThanMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString < matchString
    }

    override fun lessThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        val userString = asStringOrNull(userValue) ?: return false
        val matchString = asStringOrNull(matchValue) ?: return false
        return userString <= matchString
    }

    override fun existsMatch(userValue: Any?): Boolean {
        return userValue != null
    }

    override fun notExistsMatch(userValue: Any?): Boolean {
        return userValue == null
    }
}

internal object NumberMatcher : ValueMatcher {
    override fun inMatch(userValue: Any?, matchValue: Any): Boolean {
        val userNumber = asDoubleOrNull(userValue) ?: return false
        val matchNumber = asDoubleOrNull(matchValue) ?: return false
        return userNumber == matchNumber
    }

    override fun containsMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun startsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun endsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun greaterThanMatch(userValue: Any?, matchValue: Any): Boolean {
        val userNumber = asDoubleOrNull(userValue) ?: return false
        val matchNumber = asDoubleOrNull(matchValue) ?: return false
        return userNumber > matchNumber
    }

    override fun greaterThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        val userNumber = asDoubleOrNull(userValue) ?: return false
        val matchNumber = asDoubleOrNull(matchValue) ?: return false
        return userNumber >= matchNumber
    }

    override fun lessThanMatch(userValue: Any?, matchValue: Any): Boolean {
        val userNumber = asDoubleOrNull(userValue) ?: return false
        val matchNumber = asDoubleOrNull(matchValue) ?: return false
        return userNumber < matchNumber
    }

    override fun lessThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        val userNumber = asDoubleOrNull(userValue) ?: return false
        val matchNumber = asDoubleOrNull(matchValue) ?: return false
        return userNumber <= matchNumber
    }

    override fun existsMatch(userValue: Any?): Boolean {
        return userValue != null
    }

    override fun notExistsMatch(userValue: Any?): Boolean {
        return userValue == null
    }

}

internal object BooleanMatcher : ValueMatcher {
    override fun inMatch(userValue: Any?, matchValue: Any): Boolean {
        val userBoolean = asBooleanOrNull(userValue) ?: return false
        val matchBoolean = asBooleanOrNull(matchValue) ?: return false
        return userBoolean == matchBoolean
    }

    override fun containsMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun startsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun endsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun greaterThanMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun greaterThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun lessThanMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun lessThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun existsMatch(userValue: Any?): Boolean {
        return userValue != null
    }

    override fun notExistsMatch(userValue: Any?): Boolean {
        return userValue == null
    }
}

internal object VersionMatcher : ValueMatcher {
    override fun inMatch(userValue: Any?, matchValue: Any): Boolean {
        val userVersion = asVersionOrNull(userValue) ?: return false
        val matchVersion = asVersionOrNull(matchValue) ?: return false
        return userVersion == matchVersion
    }

    override fun containsMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun startsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun endsWithMatch(userValue: Any?, matchValue: Any): Boolean {
        return false
    }

    override fun greaterThanMatch(userValue: Any?, matchValue: Any): Boolean {
        val userVersion = asVersionOrNull(userValue) ?: return false
        val matchVersion = asVersionOrNull(matchValue) ?: return false
        return userVersion > matchVersion
    }

    override fun greaterThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        val userVersion = asVersionOrNull(userValue) ?: return false
        val matchVersion = asVersionOrNull(matchValue) ?: return false
        return userVersion >= matchVersion
    }

    override fun lessThanMatch(userValue: Any?, matchValue: Any): Boolean {
        val userVersion = asVersionOrNull(userValue) ?: return false
        val matchVersion = asVersionOrNull(matchValue) ?: return false
        return userVersion < matchVersion
    }

    override fun lessThanOrEqualToMatch(userValue: Any?, matchValue: Any): Boolean {
        val userVersion = asVersionOrNull(userValue) ?: return false
        val matchVersion = asVersionOrNull(matchValue) ?: return false
        return userVersion <= matchVersion
    }

    override fun existsMatch(userValue: Any?): Boolean {
        return userValue != null
    }

    override fun notExistsMatch(userValue: Any?): Boolean {
        return userValue == null
    }
}
