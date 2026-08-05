package io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.model.InAppMessage
import io.hackle.sdk.core.user.HackleUser

class InAppMessageFrequencyCapMatcher(
    private val storage: InAppMessageImpressionStorage,
) : InAppMessageMatcher<InAppMessageEligibilityEvaluateRequest> {
    override fun matches(request: InAppMessageEligibilityEvaluateRequest, context: Evaluator.Context): Boolean {
        return isFrequencyCapped(request.inAppMessage, request.user, request.timestamp)
    }

    private fun isFrequencyCapped(inAppMessage: InAppMessage, user: HackleUser, timestamp: Long): Boolean {
        val frequencyCap = inAppMessage.eventTrigger.frequencyCap ?: return false

        val contexts = createMatchContexts(frequencyCap)
        if (contexts.isEmpty()) {
            return false
        }

        val impressions = storage.get(inAppMessage)
        for (impression in impressions) {
            for (context in contexts) {
                if (context.matches(user, timestamp, impression)) {
                    return true
                }
            }
        }
        return false
    }

    private fun createMatchContexts(frequencyCap: InAppMessage.EventTrigger.FrequencyCap): List<MatchContext> {
        val contexts = mutableListOf<MatchContext>()
        for (identifierCap in frequencyCap.identifierCaps) {
            val predicate = IdentifierCapPredicate(identifierCap)
            contexts.add(MatchContext(predicate))
        }
        val durationCap = frequencyCap.durationCap
        if (durationCap != null) {
            contexts.add(MatchContext(DurationCapPredicate(durationCap)))
        }

        return contexts
    }

    private class MatchContext(private val predicate: FrequencyCapPredicate) {
        private var matchCount = 0

        fun matches(user: HackleUser, timestamp: Long, impression: InAppMessageImpression): Boolean {
            if (predicate.matches(user, timestamp, impression)) {
                matchCount++
            }
            return matchCount >= predicate.thresholdCount
        }
    }

    interface FrequencyCapPredicate {
        val thresholdCount: Int
        fun matches(user: HackleUser, timestamp: Long, impression: InAppMessageImpression): Boolean
    }

    class IdentifierCapPredicate(
        private val identifierCap: InAppMessage.EventTrigger.IdentifierCap,
    ) : FrequencyCapPredicate {
        override val thresholdCount: Int get() = identifierCap.count

        override fun matches(user: HackleUser, timestamp: Long, impression: InAppMessageImpression): Boolean {
            val userIdentifier = user.identifiers[identifierCap.identifierType] ?: return false
            val impressionIdentifier = impression.identifiers[identifierCap.identifierType] ?: return false
            return userIdentifier == impressionIdentifier
        }
    }

    class DurationCapPredicate(
        private val durationCap: InAppMessage.EventTrigger.DurationCap,
    ) : FrequencyCapPredicate {
        override val thresholdCount: Int get() = durationCap.count

        override fun matches(user: HackleUser, timestamp: Long, impression: InAppMessageImpression): Boolean {
            return (timestamp - impression.timestamp) <= durationCap.durationMillis
        }
    }
}
