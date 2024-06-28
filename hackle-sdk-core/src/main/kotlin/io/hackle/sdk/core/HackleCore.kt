package io.hackle.sdk.core

import io.hackle.sdk.common.Event
import io.hackle.sdk.common.ParameterConfig
import io.hackle.sdk.common.Variation
import io.hackle.sdk.common.decision.Decision
import io.hackle.sdk.common.decision.DecisionReason.*
import io.hackle.sdk.common.decision.FeatureFlagDecision
import io.hackle.sdk.common.decision.RemoteConfigDecision
import io.hackle.sdk.core.decision.InAppMessageDecision
import io.hackle.sdk.core.evaluation.EvaluationContext
import io.hackle.sdk.core.evaluation.evaluator.DelegatingEvaluator
import io.hackle.sdk.core.evaluation.evaluator.Evaluators
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluation
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentRequest
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageEvaluator
import io.hackle.sdk.core.evaluation.evaluator.inappmessage.InAppMessageRequest
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigRequest
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.evaluation.get
import io.hackle.sdk.core.evaluation.target.DelegatingManualOverrideStorage
import io.hackle.sdk.core.evaluation.target.ManualOverrideStorage
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEvent
import io.hackle.sdk.core.event.UserEventFactory
import io.hackle.sdk.core.event.process
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.internal.utils.tryClose
import io.hackle.sdk.core.model.EventType
import io.hackle.sdk.core.model.Experiment
import io.hackle.sdk.core.model.ValueType
import io.hackle.sdk.core.user.HackleUser
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * DO NOT use this module directly.
 * This module is only used internally by Hackle.
 * Backward compatibility is not supported.
 * Please use server-sdk or client-sdk instead.
 *
 * @author Yong
 */
class HackleCore internal constructor(
    private val experimentEvaluator: ExperimentEvaluator,
    private val remoteConfigEvaluator: RemoteConfigEvaluator<Any>,
    private val inAppMessageEvaluator: InAppMessageEvaluator,
    private val workspaceFetcher: WorkspaceFetcher,
    private val eventFactory: UserEventFactory,
    private val eventProcessor: EventProcessor,
    private val clock: Clock,
) : AutoCloseable {

    fun experiment(experimentKey: Long, user: HackleUser, defaultVariation: Variation): Decision {
        val workspace = workspaceFetcher.fetch() ?: return Decision.of(defaultVariation, SDK_NOT_READY)
        val experiment = workspace.getExperimentOrNull(experimentKey)
            ?: return Decision.of(defaultVariation, EXPERIMENT_NOT_FOUND)

        val request = ExperimentRequest.of(workspace, user, experiment, defaultVariation)
        val (evaluation, decision) = experiment(request)

        val events = eventFactory.create(request, evaluation)
        eventProcessor.process(events)

        return decision
    }

    fun experiments(user: HackleUser): Map<Experiment, Decision> {
        val decisions = hashMapOf<Experiment, Decision>()
        val workspace = workspaceFetcher.fetch() ?: return decisions
        for (experiment in workspace.experiments) {
            val request = ExperimentRequest.of(workspace, user, experiment, Variation.CONTROL)
            val (_, decision) = experiment(request)
            decisions[experiment] = decision
        }
        return decisions
    }

    private fun experiment(request: ExperimentRequest): Pair<ExperimentEvaluation, Decision> {
        val evaluation = experimentEvaluator.evaluate(request, Evaluators.context())
        val config = evaluation.config ?: ParameterConfig.empty()
        val decision =
            Decision.of(Variation.from(evaluation.variationKey), evaluation.reason, config, evaluation.experiment)
        return Pair(evaluation, decision)
    }

    fun featureFlag(featureKey: Long, user: HackleUser): FeatureFlagDecision {
        val workspace = workspaceFetcher.fetch() ?: return FeatureFlagDecision.off(SDK_NOT_READY)
        val featureFlag =
            workspace.getFeatureFlagOrNull(featureKey) ?: return FeatureFlagDecision.off(FEATURE_FLAG_NOT_FOUND)

        val request = ExperimentRequest.of(workspace, user, featureFlag, Variation.CONTROL)
        val (evaluation, decision) = featureFlag(request)

        val events = eventFactory.create(request, evaluation)
        eventProcessor.process(events)

        return decision
    }

    fun featureFlags(user: HackleUser): Map<Experiment, FeatureFlagDecision> {
        val decisions = hashMapOf<Experiment, FeatureFlagDecision>()
        val workspace = workspaceFetcher.fetch() ?: return decisions
        for (featureFlag in workspace.featureFlags) {
            val request = ExperimentRequest.of(workspace, user, featureFlag, Variation.CONTROL)
            val (_, decision) = featureFlag(request)
            decisions[featureFlag] = decision
        }
        return decisions
    }

    private fun featureFlag(request: ExperimentRequest): Pair<ExperimentEvaluation, FeatureFlagDecision> {
        val evaluation = experimentEvaluator.evaluate(request, Evaluators.context())
        val config = evaluation.config ?: ParameterConfig.empty()
        val decision = if (Variation.from(evaluation.variationKey).isControl) {
            FeatureFlagDecision.off(evaluation.reason, config, evaluation.experiment)
        } else {
            FeatureFlagDecision.on(evaluation.reason, config, evaluation.experiment)
        }
        return Pair(evaluation, decision)
    }


    fun track(event: Event, user: HackleUser, timestamp: Long) {
        val eventType = workspaceFetcher.fetch()?.getEventTypeOrNull(event.key) ?: EventType.Undefined(event.key)
        eventProcessor.process(UserEvent.track(eventType, event, timestamp, user))
    }

    fun <T : Any> remoteConfig(
        parameterKey: String,
        user: HackleUser,
        requiredType: ValueType,
        defaultValue: T,
    ): RemoteConfigDecision<T> {
        val workspace = workspaceFetcher.fetch() ?: return RemoteConfigDecision.of(defaultValue, SDK_NOT_READY)
        val parameter = workspace.getRemoteConfigParameterOrNull(parameterKey)
            ?: return RemoteConfigDecision.of(defaultValue, REMOTE_CONFIG_PARAMETER_NOT_FOUND)

        val request = RemoteConfigRequest(workspace, user, parameter, requiredType, defaultValue)
        val evaluation = remoteConfigEvaluator.evaluate(request, Evaluators.context())

        val events = eventFactory.create(request, evaluation)
        eventProcessor.process(events)

        @Suppress("UNCHECKED_CAST")
        return RemoteConfigDecision.of(evaluation.value, evaluation.reason) as RemoteConfigDecision<T>
    }

    fun inAppMessage(inAppMessageKey: Long, user: HackleUser): InAppMessageDecision {
        val workspace = workspaceFetcher.fetch() ?: return InAppMessageDecision.of(SDK_NOT_READY)

        val inAppMessage = workspace.getInAppMessageOrNull(inAppMessageKey)
            ?: return InAppMessageDecision.of(IN_APP_MESSAGE_NOT_FOUND)

        val request = InAppMessageRequest(workspace, user, inAppMessage, timestamp = clock.currentMillis())

        val evaluation = inAppMessageEvaluator.evaluate(request, Evaluators.context())

        val events = eventFactory.create(request, evaluation)
        eventProcessor.process(events)

        return InAppMessageDecision.of(
            evaluation.reason,
            evaluation.inAppMessage,
            evaluation.message,
            evaluation.properties
        )
    }

    fun flush() {
        eventProcessor.flush()
    }

    override fun close() {
        workspaceFetcher.tryClose()
        eventProcessor.tryClose()
    }

    companion object {
        fun create(
            context: EvaluationContext,
            workspaceFetcher: WorkspaceFetcher,
            eventProcessor: EventProcessor,
            vararg manualOverrideStorages: ManualOverrideStorage
        ): HackleCore {

            val delegatingEvaluator = DelegatingEvaluator()
            context.initialize(delegatingEvaluator, DelegatingManualOverrideStorage(manualOverrideStorages.toList()))
            val flowFactory = EvaluationFlowFactory(context)

            val experimentEvaluator = ExperimentEvaluator(flowFactory)
            val remoteConfigEvaluator = RemoteConfigEvaluator<Any>(context.get())
            val inAppMessageEvaluator = InAppMessageEvaluator(flowFactory)

            delegatingEvaluator.add(experimentEvaluator)
            delegatingEvaluator.add(remoteConfigEvaluator)

            return HackleCore(
                experimentEvaluator = experimentEvaluator,
                remoteConfigEvaluator = remoteConfigEvaluator,
                inAppMessageEvaluator = inAppMessageEvaluator,
                workspaceFetcher = workspaceFetcher,
                eventFactory = UserEventFactory(Clock.SYSTEM),
                eventProcessor = eventProcessor,
                clock = Clock.SYSTEM,
            )
        }
    }
}
