package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

fun workspace(init: WorkspaceDsl.() -> Unit = {}): Workspace {
    return WorkspaceDsl().apply(init).build()
}

class WorkspaceDsl : BucketRegistry, Workspace {

    private val experiments = mutableMapOf<Long, Experiment>()
    private val featureFlags = mutableMapOf<Long, Experiment>()
    private val eventTypes = mutableMapOf<String, EventType>()
    private val buckets = mutableMapOf<Long, Bucket>()
    private val segments = mutableMapOf<String, Segment>()

    fun experiment(
        id: Long = IdentifierGenerator.generate("experiment"),
        key: Long = IdentifierGenerator.generate("experimentKey"),
        status: Experiment.Status,
        init: ExperimentDsl.() -> Unit
    ): Experiment {
        return ExperimentDsl(id, key, AB_TEST, status, this).apply(init).build().also {
            experiments[it.key] = it
        }

    }

    fun featureFlag(
        id: Long = IdentifierGenerator.generate("experiment"),
        key: Long = IdentifierGenerator.generate("featureKey"),
        status: Experiment.Status,
        init: ExperimentDsl.() -> Unit
    ): Experiment {
        return ExperimentDsl(id, key, FEATURE_FLAG, status, this).apply(init).build().also {
            featureFlags[it.key] = it
        }
    }

    fun segment(
        id: Long = IdentifierGenerator.generate("segment"),
        key: String,
        type: Segment.Type,
        init: SegmentDsl.() -> Unit = {}
    ): Segment {
        return SegmentDsl(id, key, type).apply(init).build().also {
            segments[it.key] = it
        }
    }

    override fun getExperimentOrNull(experimentKey: Long): Experiment? = experiments[experimentKey]
    override fun getFeatureFlagOrNull(featureKey: Long): Experiment? = featureFlags[featureKey]
    override fun getEventTypeOrNull(eventTypeKey: String): EventType? = eventTypes[eventTypeKey]
    override fun getBucketOrNull(bucketId: Long): Bucket? = buckets[bucketId]
    override fun getSegmentOrNull(segmentKey: String): Segment? = segments[segmentKey]

    override fun register(bucket: Bucket) {
        buckets[bucket.id] = bucket
    }

    fun build(): Workspace {
        return this
    }
}
