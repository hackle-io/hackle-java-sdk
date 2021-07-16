package io.hackle.sdk.internal.workspace

import io.hackle.sdk.core.internal.log.Logger
import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.workspace.Workspace

/**
 * @author Yong
 */
internal class WorkspaceImpl(
    private val experiments: Map<Long, Experiment>,
    private val featureFlags: Map<Long, FeatureFlag>,
    private val eventTypes: Map<String, EventType>,
) : Workspace {

    override fun getEventTypeOrNull(eventTypeKey: String): EventType? {
        return eventTypes[eventTypeKey]
    }

    override fun getFeatureFlagOrNull(featureFlagKey: Long): FeatureFlag? {
        return featureFlags[featureFlagKey]
    }

    override fun getExperimentOrNull(experimentKey: Long): Experiment? {
        return experiments[experimentKey]
    }

    companion object {

        private val log = Logger<WorkspaceImpl>()

        fun from(dto: WorkspaceDto): Workspace {
            val buckets: Map<Long, Bucket> =
                dto.buckets.associate { it.id to it.toBucket() }

            val experiment: Map<Long, Experiment> =
                dto.experiments.asSequence()
                    .mapNotNull { it.toExperiment(buckets.getValue(it.bucketId)) }
                    .associateBy { it.key }

            val featureFlags = dto.featureFlags.asSequence()
                .map { it.toFeatureFlag(buckets.getValue(it.bucketId)) }
                .associateBy { it.key }

            val eventTypes: Map<String, EventType.Custom> = dto.events.associate { it.key to it.toEventType() }

            return WorkspaceImpl(
                experiments = experiment,
                featureFlags = featureFlags,
                eventTypes = eventTypes
            )
        }

        private fun ExperimentDto.toExperiment(bucket: Bucket): Experiment? {

            val variations = variations.associate { it.id to it.toVariation() }
            val overrides = execution.userOverrides.associate { it.userId to it.variationId }

            return when (execution.status) {
                "READY" -> Experiment.Ready(
                    id = id,
                    key = key,
                    variations = variations,
                    overrides = overrides
                )
                "RUNNING" -> Experiment.Running(
                    id = id,
                    key = key,
                    bucket = bucket,
                    variations = variations,
                    overrides = overrides
                )
                "PAUSED" -> Experiment.Paused(
                    id = id,
                    key = key,
                    variations = variations,
                    overrides = overrides
                )
                "STOPPED" -> Experiment.Completed(
                    id = id,
                    key = key,
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

        private fun FeatureFlagDto.toFeatureFlag(bucket: Bucket): FeatureFlag {
            val variations = variations.associate { it.id to it.toVariation() }
            val overrides = execution.userOverrides.associate { it.userId to it.variationId }
            return FeatureFlag(
                id = id,
                key = key,
                bucket = bucket,
                variations = variations,
                overrides = overrides
            )
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
