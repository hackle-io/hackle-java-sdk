package io.hackle.sdk.core.evaluation.evaluator.inappmessage.layout

import io.hackle.sdk.core.evaluation.evaluator.ContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutEvaluator(
    private val experimentEvaluator: InAppMessageExperimentEvaluator,
    private val selector: InAppMessageLayoutSelector,
) : ContextualEvaluator<InAppMessageLayoutRequest, InAppMessageLayoutEvaluation>() {
    override fun supports(request: Evaluator.Request): Boolean {
        return request is InAppMessageLayoutRequest
    }

    override fun evaluateInternal(
        request: InAppMessageLayoutRequest,
        context: Evaluator.Context,
    ): InAppMessageLayoutEvaluation {
        val experimentContext = request.inAppMessage.messageContext.experimentContext
        val message = if (experimentContext != null) {
            evaluateExperiment(request, context, experimentContext)
        } else {
            evaluateDefault(request, context)
        }
        return InAppMessageLayoutEvaluation.of(request, context, message)
    }

    private fun evaluateDefault(request: InAppMessageLayoutRequest, context: Evaluator.Context): InAppMessage.Message {
        val langCondition = LangCondition(request.inAppMessage.messageContext.defaultLang)
        return selector.select(request.inAppMessage, langCondition)
    }

    private fun evaluateExperiment(
        request: InAppMessageLayoutRequest,
        context: Evaluator.Context,
        experimentContext: InAppMessage.ExperimentContext,
    ): InAppMessage.Message {
        val experiment =
            requireNotNull(request.workspace.getExperimentOrNull(experimentContext.key)) { "Experiment[key=${experimentContext.key}]" }
        val experimentEvaluation = experimentEvaluator.evaluate(request, context, experiment)

        val langCondition = LangCondition(request.inAppMessage.messageContext.defaultLang)
        val experimentCondition = ExperimentCondition(experimentEvaluation.variationKey)
        return selector.select(request.inAppMessage) { langCondition(it) && experimentCondition(it) }
    }

    private class LangCondition(private val lang: String) : (InAppMessage.Message) -> Boolean {
        override fun invoke(message: InAppMessage.Message): Boolean {
            return this.lang == message.lang
        }
    }

    private class ExperimentCondition(private val variationKey: String) : (InAppMessage.Message) -> Boolean {
        override fun invoke(message: InAppMessage.Message): Boolean {
            return this.variationKey == message.variationKey
        }
    }
}
