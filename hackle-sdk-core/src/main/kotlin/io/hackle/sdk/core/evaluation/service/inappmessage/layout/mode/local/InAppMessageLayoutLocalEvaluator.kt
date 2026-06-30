package io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local

import io.hackle.sdk.common.decision.DecisionReason
import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.mode.local.LocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResult
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.match.InAppMessageLayoutExperimentEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.match.InAppMessageLayoutSelector
import io.hackle.sdk.core.model.InAppMessage

class InAppMessageLayoutLocalEvaluator(
    private val experimentEvaluator: InAppMessageLayoutExperimentEvaluator,
    private val selector: InAppMessageLayoutSelector,
    private val eventRecorder: EvaluationEventRecorder,
) : LocalEvaluator<InAppMessageLayoutLocalEvaluateRequest, InAppMessageLayoutEvaluateResponse>(),
    InAppMessageLayoutEvaluator<InAppMessageLayoutLocalEvaluateRequest> {

    override fun supports(request: EvaluateRequest): Boolean {
        return request is InAppMessageLayoutLocalEvaluateRequest
    }

    override fun doEvaluate(
        request: InAppMessageLayoutLocalEvaluateRequest,
        context: Evaluator.Context,
    ): InAppMessageLayoutEvaluateResponse {
        val experimentContext = request.entity.experimentContext
        val message = if (experimentContext != null) {
            evaluateExperiment(request, context, experimentContext)
        } else {
            evaluateDefault(request)
        }
        val result = InAppMessageLayoutEvaluateResult.of(DecisionReason.IN_APP_MESSAGE_TARGET, message)
        return InAppMessageLayoutEvaluateResponse.of(request, context, result)
    }

    private fun evaluateDefault(request: InAppMessageLayoutLocalEvaluateRequest): InAppMessage.Message {
        val langCondition = LangCondition(request.entity.messageContext.defaultLang)
        return selector.select(request.entity, langCondition)
    }

    private fun evaluateExperiment(
        request: InAppMessageLayoutLocalEvaluateRequest,
        context: Evaluator.Context,
        experimentContext: InAppMessage.ExperimentContext,
    ): InAppMessage.Message {
        val experiment =
            requireNotNull(request.workspace.getExperimentOrNull(experimentContext.key)) { "Experiment[key=${experimentContext.key}]" }
        val experimentEvaluation = experimentEvaluator.evaluate(request, context, experiment)

        val langCondition = LangCondition(request.entity.messageContext.defaultLang)
        val experimentCondition = ExperimentCondition(experimentEvaluation.result.variationKey)
        return selector.select(request.entity) { langCondition(it) && experimentCondition(it) }
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

    override fun record(
        request: InAppMessageLayoutLocalEvaluateRequest,
        response: InAppMessageLayoutEvaluateResponse,
    ) {
        eventRecorder.record(response)
    }
}
