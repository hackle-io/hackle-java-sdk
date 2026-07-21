package io.hackle.sdk.core.evaluation

import io.hackle.sdk.core.HackleCoreContext
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.evaluator.Evaluator
import io.hackle.sdk.core.evaluation.evaluator.EvaluatorFactory
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.event.EvaluationEventFactory
import io.hackle.sdk.core.evaluation.event.EvaluationEventRecorder
import io.hackle.sdk.core.evaluation.match.ConditionMatcherFactory
import io.hackle.sdk.core.evaluation.match.TargetMatcher
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateRequest
import io.hackle.sdk.core.evaluation.service.experiment.ExperimentEvaluateResponse
import io.hackle.sdk.core.evaluation.service.experiment.flow.ExperimentLocalEvaluationFlowFactory
import io.hackle.sdk.core.evaluation.service.experiment.match.ExperimentManualOverrideStorage
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentLocalEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.mode.local.ExperimentReferenceLocalEvaluator
import io.hackle.sdk.core.evaluation.service.experiment.mode.remote.ExperimentRemoteEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.InAppMessageEligibilityEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityLocalEvaluationFlowFactory
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.flow.InAppMessageEligibilityRemoteEvaluationFlowFactory
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageHiddenStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.match.InAppMessageImpressionStorage
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.local.InAppMessageEligibilityLocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.eligibility.mode.remote.InAppMessageEligibilityRemoteEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateRequest
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.InAppMessageLayoutEvaluateResponse
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.match.InAppMessageLayoutSelector
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.local.InAppMessageLayoutLocalEvaluator
import io.hackle.sdk.core.evaluation.service.inappmessage.layout.mode.remote.InAppMessageLayoutRemoteEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateRequest
import io.hackle.sdk.core.evaluation.service.remoteconfig.RemoteConfigEvaluateResponse
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleDeterminer
import io.hackle.sdk.core.evaluation.service.remoteconfig.match.RemoteConfigParameterTargetRuleMatcher
import io.hackle.sdk.core.evaluation.service.remoteconfig.mode.local.RemoteConfigLocalEvaluator
import io.hackle.sdk.core.evaluation.service.remoteconfig.mode.remote.RemoteConfigRemoteEvaluator
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.internal.time.Clock

class EvaluateProcessor(
    private val evaluatorFactory: EvaluatorFactory,
) {
    fun experiment(request: ExperimentEvaluateRequest): ExperimentEvaluateResponse {
        val evaluator = evaluatorFactory.experiment(request)
        return evaluate(evaluator, request)
    }

    fun remoteConfig(request: RemoteConfigEvaluateRequest): RemoteConfigEvaluateResponse {
        val evaluator = evaluatorFactory.remoteConfig(request)
        return evaluate(evaluator, request)
    }

    fun inAppMessage(request: InAppMessageEligibilityEvaluateRequest): InAppMessageEligibilityEvaluateResponse {
        val evaluator = evaluatorFactory.inAppMessage(request)
        return evaluate(evaluator, request)
    }

    fun inAppMessage(request: InAppMessageLayoutEvaluateRequest): InAppMessageLayoutEvaluateResponse {
        val evaluator = evaluatorFactory.inAppMessage(request)
        return evaluate(evaluator, request)
    }

    private fun <REQUEST : EvaluateRequest, RESPONSE : EvaluateResponse> evaluate(
        evaluator: Evaluator<REQUEST, RESPONSE>,
        request: REQUEST,
    ): RESPONSE {
        val response = evaluator.evaluate(request, Evaluators.context())
        if (request.record) {
            evaluator.record(request, response)
        }
        return response
    }

    companion object {
        fun create(
            context: HackleCoreContext,
            clock: Clock,
            eventProcessor: EventProcessor,
            overrideStorage: ExperimentManualOverrideStorage,
            impressionStorage: InAppMessageImpressionStorage,
            hiddenStorage: InAppMessageHiddenStorage,
        ): EvaluateProcessor {
            val evaluatorFactory = EvaluatorFactory()

            val eventFactory = EvaluationEventFactory(
                clock = clock
            )
            val eventRecorder = EvaluationEventRecorder(
                eventFactory = eventFactory,
                eventProcessor = eventProcessor
            )


            val targetMatcher = TargetMatcher(
                conditionMatcherFactory = ConditionMatcherFactory(
                    evaluatorFactory = evaluatorFactory,
                    clock = clock
                )
            )
            context.register(targetMatcher)

            val bucketer = Bucketer()


            // ===== Local =====

            val experimentLocalEvaluator = ExperimentLocalEvaluator(
                evaluationFlowFactory = ExperimentLocalEvaluationFlowFactory(
                    targetMatcher = targetMatcher,
                    bucketer = bucketer,
                    overrideStorage = overrideStorage
                ),
                eventRecorder = eventRecorder
            )
            val remoteConfigLocalEvaluator = RemoteConfigLocalEvaluator(
                targetRuleDeterminer = RemoteConfigParameterTargetRuleDeterminer(
                    matcher = RemoteConfigParameterTargetRuleMatcher(
                        targetMatcher = targetMatcher,
                        bucketer = bucketer
                    )
                ),
                eventRecorder = eventRecorder
            )
            val inAppMessageLayoutLocalEvaluator = InAppMessageLayoutLocalEvaluator(
                experimentEvaluator = ExperimentReferenceLocalEvaluator(
                    evaluatorFactory = evaluatorFactory
                ),
                selector = InAppMessageLayoutSelector(),
                eventRecorder = eventRecorder
            )
            val inAppMessageEligibilityLocalEvaluator = InAppMessageEligibilityLocalEvaluator(
                evaluationFlowFactory = InAppMessageEligibilityLocalEvaluationFlowFactory(
                    targetMatcher = targetMatcher,
                    impressionStorage = impressionStorage,
                    hiddenStorage = hiddenStorage,
                    layoutEvaluator = inAppMessageLayoutLocalEvaluator
                ),
                eventRecorder = eventRecorder
            )

            evaluatorFactory.add(experimentLocalEvaluator)
            evaluatorFactory.add(remoteConfigLocalEvaluator)
            evaluatorFactory.add(inAppMessageLayoutLocalEvaluator)
            evaluatorFactory.add(inAppMessageEligibilityLocalEvaluator)

            // ===== Remote =====

            val experimentRemoteEvaluator = ExperimentRemoteEvaluator(
                eventRecorder = eventRecorder
            )

            val remoteConfigRemoteEvaluator = RemoteConfigRemoteEvaluator(
                eventRecorder = eventRecorder
            )
            val inAppMessageLayoutRemoteEvaluator = InAppMessageLayoutRemoteEvaluator(
                eventRecorder = eventRecorder
            )
            val inAppMessageEligibilityRemoteEvaluator = InAppMessageEligibilityRemoteEvaluator(
                evaluationFlowFactory = InAppMessageEligibilityRemoteEvaluationFlowFactory(
                    impressionStorage = impressionStorage,
                    hiddenStorage = hiddenStorage,
                    layoutEvaluator = inAppMessageLayoutRemoteEvaluator
                ),
                eventRecorder = eventRecorder
            )

            evaluatorFactory.add(experimentRemoteEvaluator)
            evaluatorFactory.add(remoteConfigRemoteEvaluator)
            evaluatorFactory.add(inAppMessageLayoutRemoteEvaluator)
            evaluatorFactory.add(inAppMessageEligibilityRemoteEvaluator)

            return EvaluateProcessor(evaluatorFactory)
        }
    }
}
