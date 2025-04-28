package io.hackle.sdk.core.evaluation.target

import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentContextualEvaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.InAppMessage

internal class InAppMessageResolver(
    private val experimentEvaluator: InAppMessageExperimentEvaluator,
    private val messageSelector: InAppMessageSelector
) {

    fun resolve(request: InAppMessageRequest, context: Evaluator.Context): InAppMessage.Message {
        val experiment = experiment(request)
        if(experiment == null) {
            return messageSelector.select(request) { message ->
                message.lang == request.inAppMessage.messageContext.defaultLang
            }
        }
        
        val evaluation = this.experimentEvaluator.evaluate(request, context, experiment)
        return messageSelector.select(request) { message ->
            message.lang == request.inAppMessage.messageContext.defaultLang &&
                    evaluation.variationKey == message.variationKey
        }
    }
    
    private fun experiment(request: InAppMessageRequest): Experiment? {
        val experimentContext = request.inAppMessage.messageContext.experimentContext ?: return null
        return requireNotNull(request.workspace.getExperimentOrNull(experimentContext.key)) { "Experiment[key=${experimentContext.key}]" }
    }

    internal class InAppMessageSelector {
        fun select(
            request: InAppMessageRequest,
            condition: (InAppMessage.Message) -> Boolean,
        ): InAppMessage.Message {
            val message = request.inAppMessage.messageContext.messages.find(condition)
            return requireNotNull(message) { "InAppMessage must be decided [${request.inAppMessage.key}]" }
        }
    }

    internal class InAppMessageExperimentEvaluator(
        evaluator: Evaluator
    ): ExperimentContextualEvaluator(evaluator) {
        override fun decorate(
            request: Evaluator.Request,
            context: Evaluator.Context,
            evaluation: Evaluator.Evaluation
        ): ExperimentEvaluation {
            require(evaluation is ExperimentEvaluation) { "Unexpected evaluation [expected=ExperimentEvaluation, actual=${evaluation::class.java.simpleName}]" }
            
            context.setProperty("experiment_id", evaluation.experiment.id)
            context.setProperty("experiment_key", evaluation.experiment.key)
            context.setProperty("variation_id", evaluation.variationId)
            context.setProperty("variation_key", evaluation.variationKey)
            context.setProperty("experiment_decision_reason", evaluation.reason.name)

            return evaluation
        }
    }
}
