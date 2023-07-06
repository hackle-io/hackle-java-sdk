package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.event.properties
import io.hackle.sdk.core.model.Target


internal class EventConditionMatcher(
    private val eventValueResolver: EventValueResolver,
    private val valueOperatorMatcher: ValueOperatorMatcher
) : ConditionMatcher {

    override fun matches(
        request: Evaluator.Request,
        context: Evaluator.Context,
        condition: Target.Condition
    ): Boolean {
        if (request !is Evaluator.EventRequest) {
            return false
        }
        val eventValue = eventValueResolver.resolveOrNull(request.event, condition.key) ?: return false

        return valueOperatorMatcher.matches(eventValue, condition.match)
    }
}

internal class EventValueResolver {

    fun resolveOrNull(event: UserEvent, key: Target.Key): Any? {
        return when (key.type) {
            Target.Key.Type.EVENT_PROPERTY -> event.properties[key.name]
            Target.Key.Type.USER_ID,
            Target.Key.Type.USER_PROPERTY,
            Target.Key.Type.HACKLE_PROPERTY,
            Target.Key.Type.SEGMENT,
            Target.Key.Type.AB_TEST,
            Target.Key.Type.FEATURE_FLAG -> throw IllegalArgumentException("Unsupported target key Type for EventValueResolver [${key.type}]")
        }
    }
}
