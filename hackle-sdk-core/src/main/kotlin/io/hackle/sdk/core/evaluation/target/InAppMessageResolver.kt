package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.model.InAppMessage


internal class InAppMessageResolver(
    private val evaluator: Evaluator,
) {

    fun resolve(request: InAppMessageRequest, context: Evaluator.Context): InAppMessage.Message {
        return resolveExperiment(request, context)
            ?: resolveDefault(request)
    }

    private fun resolveExperiment(request: InAppMessageRequest, context: Evaluator.Context): InAppMessage.Message? {
        val experimentContext = request.inAppMessage.messageContext.experimentContext ?: return null
        val experiment = requireNotNull(request.workspace.getExperimentOrNull(experimentContext.key)) { "Experiment[key=${experimentContext.key}]"}

        val experimentRequest = ExperimentRequest.of(requestedBy = request, experiment = experiment)
        val evaluation = evaluator.evaluate(experimentRequest, context)
        require(evaluation is ExperimentEvaluation) { "Unexpected evaluation [expected=ExperimentEvaluation, actual=${evaluation::class.java.simpleName}]" }
        addExperimentContext(evaluation, context)

        val lang = request.inAppMessage.messageContext.defaultLang
        return resolveMessage(request) { it.lang == lang && evaluation.variationKey == it.variationKey }
    }

    private fun addExperimentContext(evaluation: ExperimentEvaluation, context: Evaluator.Context) {
        context.add(evaluation)
        context.setProperty("experiment_id", evaluation.experiment.id)
        context.setProperty("experiment_key", evaluation.experiment.key)
        context.setProperty("variation_id", evaluation.variationId)
        context.setProperty("variation_key", evaluation.variationKey)
        context.setProperty("experiment_decision_reason", evaluation.reason.name)
    }

    private fun resolveDefault(request: InAppMessageRequest): InAppMessage.Message {
        val long = request.inAppMessage.messageContext.defaultLang
        return resolveMessage(request) { it.lang == long }
    }

    private fun resolveMessage(
        request: InAppMessageRequest,
        condition: (InAppMessage.Message) -> Boolean,
    ): InAppMessage.Message {
        val message = request.inAppMessage.messageContext.messages.find(condition)
        return requireNotNull(message) { "InAppMessage must be decided [${request.inAppMessage.key}]" }
    }
}

internal interface InAppMessageMatcher {
    fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean
}

internal class InAppMessageUserOverrideMatcher : InAppMessageMatcher {
    override fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        return request.inAppMessage.targetContext.overrides.any { isUserOverridden(request, it) }
    }

    private fun isUserOverridden(request: InAppMessageRequest, userOverride: InAppMessage.UserOverride): Boolean {
        val identifier = request.user.identifiers[userOverride.identifierType] ?: return false
        return identifier in userOverride.identifiers
    }
}

internal class InAppMessageTargetMatcher(
    private val targetMatcher: TargetMatcher,
) : InAppMessageMatcher {
    override fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        return targetMatcher.anyMatches(request, context, request.inAppMessage.targetContext.targets)
    }
}

internal class InAppMessageHiddenMatcher(
    private val storage: InAppMessageHiddenStorage,
) : InAppMessageMatcher {
    override fun matches(request: InAppMessageRequest, context: Evaluator.Context): Boolean {
        return storage.exist(request.inAppMessage, request.timestamp)
    }
}
