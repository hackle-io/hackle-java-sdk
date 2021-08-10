package io.hackle.sdk.core

import io.hackle.sdk.core.client.HackleInternalClient
import io.hackle.sdk.core.evaluation.Evaluator
import io.hackle.sdk.core.evaluation.action.ActionDeterminer
import io.hackle.sdk.core.evaluation.action.ActionResolver
import io.hackle.sdk.core.evaluation.bucket.Bucketer
import io.hackle.sdk.core.evaluation.match.*
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

fun HackleCore.client(workspaceFetcher: WorkspaceFetcher, eventProcessor: EventProcessor): HackleInternalClient =
    HackleInternalClient(
        evaluator = Evaluator(
            actionDeterminer = ActionDeterminer(
                ruleMatcher = ExperimentRuleMatcher(
                    targetMatcher = TargetMatcher(
                        conditionMatcherFactory = with(PropertyMatcher(ValueOperatorMatcher())) {
                            ConditionMatcherFactory(
                                propertyMatcher = this,
                                segmentMatcher = SegmentMatcher(this)
                            )
                        }
                    )
                )
            ),
            actionResolver = ActionResolver(
                bucketer = Bucketer()
            )
        ),
        workspaceFetcher = workspaceFetcher,
        eventProcessor = eventProcessor
    )
