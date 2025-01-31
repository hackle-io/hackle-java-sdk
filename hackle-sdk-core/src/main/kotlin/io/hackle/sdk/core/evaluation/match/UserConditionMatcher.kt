package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.*
import io.hackle.sdk.core.user.HackleUser

internal class UserConditionMatcher(
    private val userValueResolver: UserValueResolver,
    private val valueOperatorMatcher: ValueOperatorMatcher
) : ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        val userValue = userValueResolver.resolveOrNull(request.user, condition.key) ?: return false
        return valueOperatorMatcher.matches(userValue, condition.match)
    }
}

internal class UserValueResolver {
    fun resolveOrNull(user: HackleUser, key: Target.Key): Any? {
        return when (key.type) {
            USER_ID -> user.identifiers[key.name]
            USER_PROPERTY -> user.properties[key.name]
            HACKLE_PROPERTY -> user.hackleProperties[key.name]
            SEGMENT, AB_TEST, FEATURE_FLAG, EVENT_PROPERTY, COHORT, NUMBER_OF_EVENTS_IN_DAYS, NUMBER_OF_EVENT_WITH_PROPERTY_IN_DAYS -> throw IllegalArgumentException("Unsupported target.key.type [${key.type}]")
        }
    }
}
