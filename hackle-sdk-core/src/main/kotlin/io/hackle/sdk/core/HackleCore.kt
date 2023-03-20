package io.hackle.sdk.core

import io.hackle.sdk.core.client.HackleInternalClient
import io.hackle.sdk.core.evaluation.Evaluator
import io.hackle.sdk.core.evaluation.flow.EvaluationFlowFactory
import io.hackle.sdk.core.evaluation.target.DelegatingManualOverrideStorage
import io.hackle.sdk.core.evaluation.target.ManualOverrideStorage
import io.hackle.sdk.core.event.EventProcessor
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
    return HackleInternalClient(
        evaluator = Evaluator(EvaluationFlowFactory(DelegatingManualOverrideStorage(manualOverrideStorages.toList()))),
        workspaceFetcher = workspaceFetcher,
        eventProcessor = eventProcessor
    )
}

