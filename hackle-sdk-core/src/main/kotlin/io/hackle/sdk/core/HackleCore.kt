package io.hackle.sdk.core

import io.hackle.sdk.core.client.HackleInternalClient
import io.hackle.sdk.core.evaluation.evaluator.DelegatingEvaluator
import io.hackle.sdk.core.evaluation.evaluator.experiment.ExperimentEvaluator
import io.hackle.sdk.core.evaluation.evaluator.remoteconfig.RemoteConfigEvaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.evaluation.target.DelegatingManualOverrideStorage
import io.hackle.sdk.core.evaluation.target.ManualOverrideStorage
import io.hackle.sdk.core.event.EventProcessor
import io.hackle.sdk.core.event.UserEventFactory
import io.hackle.sdk.core.internal.time.Clock
import io.hackle.sdk.core.workspace.WorkspaceFetcher

/**
 * DO NOT use this module directly.
 * This module is only used internally by Hackle.
 * Backward compatibility is not supported.
 * Please use server-sdk or client-sdk instead.
 *
 * @author Yong
 */
object HackleCore

fun HackleCore.client(
    workspaceFetcher: WorkspaceFetcher,
    eventProcessor: EventProcessor,
    vararg manualOverrideStorages: ManualOverrideStorage
): HackleInternalClient {

    val delegatingEvaluator = DelegatingEvaluator()

    val manualOverrideStorage = DelegatingManualOverrideStorage(manualOverrideStorages.toList())
    val flowFactory = EvaluationFlowFactory(delegatingEvaluator, manualOverrideStorage)

    val experimentEvaluator = ExperimentEvaluator(flowFactory)
        .also { delegatingEvaluator.add(it) }
    val remoteConfigEvaluator = RemoteConfigEvaluator<Any>(flowFactory.remoteConfigParameterTargetRuleDeterminer)
        .also { delegatingEvaluator.add(it) }

    return HackleInternalClient(
        experimentEvaluator,
        remoteConfigEvaluator,
        workspaceFetcher,
        UserEventFactory(Clock.SYSTEM),
        eventProcessor
    )
}
