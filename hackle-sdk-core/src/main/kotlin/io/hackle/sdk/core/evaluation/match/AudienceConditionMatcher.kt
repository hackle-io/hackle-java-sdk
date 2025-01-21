package io.hackle.sdk.core.evaluation.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.model.Target.Key.Type.NUMBER_OF_EVENTS_IN_DAYS

/**
 * AudienceConditionMatcher
 *
 * 실시간 타겟팅
 * TODO: 명칭 결정되면 객체명 수정 필요
 */
internal class AudienceConditionMatcher(
): ConditionMatcher {
    override fun matches(request: Evaluator.Request, context: Evaluator.Context, condition: Target.Condition): Boolean {
        require(condition.key.type == NUMBER_OF_EVENTS_IN_DAYS) { "Unsupported Target.Key.Type [${condition.key.type}]" }
        // TODO: Not yet implemented
        return false
    }
}
