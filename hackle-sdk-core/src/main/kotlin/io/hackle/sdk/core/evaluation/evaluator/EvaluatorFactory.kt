package io.hackle.sdk.core.evaluation.evaluator

import io.hackle.sdk.core.evaluation.EvaluateRequest
import io.hackle.sdk.core.evaluation.EvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluator

class EvaluatorFactory {

    private val evaluators = mutableListOf<ContextualEvaluator<EvaluateRequest, EvaluateResponse>>()

    fun add(evaluator: ContextualEvaluator<out EvaluateRequest, out EvaluateResponse>) {
        @Suppress("UNCHECKED_CAST")
        evaluators.add(evaluator as ContextualEvaluator<EvaluateRequest, EvaluateResponse>)
    }

    fun get(request: EvaluateRequest): Evaluator<EvaluateRequest, EvaluateResponse> {
        val evaluator = evaluators.find { it.supports(request) }
        return requireNotNull(evaluator)
    }

    fun experiment(request: ExperimentEvaluateRequest): ExperimentEvaluator<ExperimentEvaluateRequest> {
        @Suppress("UNCHECKED_CAST")
        return get(request) as ExperimentEvaluator<ExperimentEvaluateRequest>
    }

    fun <T : Any> remoteConfig(request: RemoteConfigEvaluateRequest<T>): RemoteConfigEvaluator<T, RemoteConfigEvaluateRequest<T>> {
        @Suppress("UNCHECKED_CAST")
        return get(request) as RemoteConfigEvaluator<T, RemoteConfigEvaluateRequest<T>>
    }

    fun inAppMessage(request: InAppMessageEligibilityEvaluateRequest): InAppMessageEligibilityEvaluator<InAppMessageEligibilityEvaluateRequest> {
        @Suppress("UNCHECKED_CAST")
        return get(request) as InAppMessageEligibilityEvaluator<InAppMessageEligibilityEvaluateRequest>
    }

    fun inAppMessage(request: InAppMessageLayoutEvaluateRequest): InAppMessageLayoutEvaluator<InAppMessageLayoutEvaluateRequest> {
        @Suppress("UNCHECKED_CAST")
        return get(request) as InAppMessageLayoutEvaluator<InAppMessageLayoutEvaluateRequest>
    }
}
