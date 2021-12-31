package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

fun workspace(init: WorkspaceDsl.() -> Unit): Workspace {
    return WorkspaceDsl().apply(init).build()
}

class WorkspaceDsl : BucketRegistry, Workspace {

    private val experiments = mutableMapOf<Long, Experiment>()
    private val featureFlags = mutableMapOf<Long, Experiment>()
    private val eventTypes = mutableMapOf<String, EventType>()
    private val buckets = mutableMapOf<Long, Bucket>()

    fun experiment(
        id: Long = IdentifierGenerator.generate("experiment"),
        key: Long = IdentifierGenerator.generate("experimentKey"),
        status: Experiment.Status,
        init: ExperimentDsl.() -> Unit
    ) {
        val experiment = ExperimentDsl(id, key, AB_TEST, status, this).apply(init).build()
        experiments[experiment.key] = experiment
    }

    fun featureFlag(
        id: Long = IdentifierGenerator.generate("experiment"),
        key: Long = IdentifierGenerator.generate("featureKey"),
        status: Experiment.Status,
        init: ExperimentDsl.() -> Unit
    ) {
        val featureFlag = ExperimentDsl(id, key, FEATURE_FLAG, status, this).apply(init).build()
        featureFlags[featureFlag.key] = featureFlag
    }

    override fun getExperimentOrNull(experimentKey: Long): Experiment? = experiments[experimentKey]
    override fun getFeatureFlagOrNull(featureKey: Long): Experiment? = featureFlags[featureKey]
    override fun getEventTypeOrNull(eventTypeKey: String): EventType? = eventTypes[eventTypeKey]
    override fun getBucketOrNull(bucketId: Long): Bucket? = buckets[bucketId]

    override fun register(bucket: Bucket) {
        buckets[bucket.id] = bucket
    }

    fun build(): Workspace {
        return this
    }
}
