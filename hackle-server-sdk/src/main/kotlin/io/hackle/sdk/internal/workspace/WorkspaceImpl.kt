package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG
import io.hackle.sdk.core.model.Target
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class WorkspaceImpl(
    private val experiments: Map<Long, Experiment>,
    private val featureFlags: Map<Long, Experiment>,
    private val eventTypes: Map<String, EventType>,
    private val buckets: Map<Long, Bucket>,
    private val segments: Map<Long, Segment>
) : Workspace {

    override fun getEventTypeOrNull(eventTypeKey: String): EventType? {
        return eventTypes[eventTypeKey]
    }

    override fun getFeatureFlagOrNull(featureKey: Long): Experiment? {
        return featureFlags[featureKey]
    }

    override fun getExperimentOrNull(experimentKey: Long): Experiment? {
        return experiments[experimentKey]
    }

    override fun getBucketOrNull(bucketId: Long): Bucket? {
        return buckets[bucketId]
    }

    override fun getSegmentOrNull(segmentId: Long): Segment? {
        return segments[segmentId]
    }

    companion object {

        private val log = Logger<WorkspaceImpl>()

        fun from(dto: WorkspaceDto): Workspace {
            val buckets: Map<Long, Bucket> =
                dto.buckets.associate { it.id to it.toBucket() }

            val experiment: Map<Long, Experiment> =
                dto.experiments.asSequence()
                    .mapNotNull { it.toExperiment(AB_TEST) }
                    .associateBy { it.key }

            val featureFlags: Map<Long, Experiment> =
                dto.featureFlags.asSequence()
                    .mapNotNull { it.toExperiment(FEATURE_FLAG) }
                    .associateBy { it.key }

            val eventTypes: Map<String, EventType.Custom> = dto.events.associate { it.key to it.toEventType() }

            return WorkspaceImpl(
                experiments = experiment,
                featureFlags = featureFlags,
                eventTypes = eventTypes,
                buckets = buckets,
                segments = emptyMap()
            )
        }

        private fun ExperimentDto.toExperiment(type: Experiment.Type): Experiment? {

            val variations = variations.associate { it.id to it.toVariation() }
            val overrides = execution.userOverrides.associate { it.userId to it.variationId }

            return when (execution.status) {
                "READY" -> Experiment.Draft(
                    id = id,
                    key = key,
                    type = type,
                    variations = variations,
                    overrides = overrides
                )
                "RUNNING" -> Experiment.Running(
                    id = id,
                    key = key,
                    type = type,
                    variations = variations,
                    overrides = overrides,
                    rules = listOf(
                        TargetRule(
                            target = Target(
                                conditions = listOf(
                                    Target.Condition(
                                        key = Target.Key(
                                            type = Target.Key.Type.USER_PROPERTY,
                                            name = "age"
                                        ),
                                        match = Target.Match(
                                            type = Target.Match.Type.MATCH,
                                            operator = Target.Match.Operator.GTE,
                                            valueType = Target.Match.ValueType.NUMBER,
                                            values = listOf(20)
                                        )
                                    ),
                                    Target.Condition(
                                        key = Target.Key(
                                            type = Target.Key.Type.USER_PROPERTY,
                                            name = "grade"
                                        ),
                                        match = Target.Match(
                                            type = Target.Match.Type.MATCH,
                                            operator = Target.Match.Operator.IN,
                                            valueType = Target.Match.ValueType.STRING,
                                            values = listOf("GOLD")
                                        )
                                    )
                                )
                            ),
                            action = Action.Bucket(bucketId)
                        ),
                    ),
                    defaultAction = Action.Bucket(bucketId)
                )
                "PAUSED" -> Experiment.Paused(
                    id = id,
                    key = key,
                    type = type,
                    variations = variations,
                    overrides = overrides
                )
                "STOPPED" -> Experiment.Completed(
                    id = id,
                    key = key,
                    type = type,
                    variations = variations,
                    overrides = overrides,
                    winnerVariationId = requireNotNull(winnerVariationId)
                )
                else -> {
                    log.warn { "Unknown experiment status [$status]" }
                    null
                }
            }
        }

        private fun VariationDto.toVariation() = Variation(
            id = id,
            key = key,
            isDropped = status == "DROPPED"
        )

        private fun BucketDto.toBucket() = Bucket(
            seed = seed,
            slotSize = slotSize,
            slots = slots.map { it.toSlot() }
        )

        private fun SlotDto.toSlot() = Slot(
            startInclusive = startInclusive,
            endExclusive = endExclusive,
            variationId = variationId
        )

        private fun EventTypeDto.toEventType() = EventType.Custom(id, key)
    }
}
