package io.hackle.sdk.core.workspace

import io.hackle.sdk.core.model.*
import io.hackle.sdk.core.model.Experiment.Type.AB_TEST
import io.hackle.sdk.core.model.Experiment.Type.FEATURE_FLAG

fun workspace(init: WorkspaceDsl.() -> Unit = {}): Workspace {
    return WorkspaceDsl().apply(init).build()
}

class WorkspaceDsl() : BucketRegistry, Workspace {

    private val _inAppMessages = mutableMapOf<Long, InAppMessage>()
    private val _experiments = mutableMapOf<Long, Experiment>()
    private val _featureFlags = mutableMapOf<Long, Experiment>()
    private val eventTypes = mutableMapOf<String, EventType>()
    private val buckets = mutableMapOf<Long, Bucket>()
    private val segments = mutableMapOf<String, Segment>()
    private val containers = mutableMapOf<Long, Container>()
    private val parameterConfigurations = mutableMapOf<Long, ParameterConfiguration>()

    override val id: Long get() = 1
    override val environmentId: Long get() = 1
    override val experiments: List<Experiment> get() = _experiments.values.toList()
    override val featureFlags: List<Experiment> get() = _featureFlags.values.toList()
    override val inAppMessages: List<InAppMessage> get() = _inAppMessages.values.toList()
    fun experiment(
        id: Long = IdentifierGenerator.generate("experiment"),
        key: Long = IdentifierGenerator.generate("experimentKey"),
        identifierType: String = "\$id",
        version: Int = 1,
        executionVersion: Int = 1,
        status: Experiment.Status = Experiment.Status.RUNNING,
        init: ExperimentDsl.() -> Unit
    ): Experiment {
        return ExperimentDsl(
            id,
            key,
            AB_TEST,
            identifierType,
            status,
            version,
            executionVersion,
            null,
            this
        ).apply(init).build().also {
            _experiments[it.key] = it
        }
    }

    fun featureFlag(
        id: Long = IdentifierGenerator.generate("experiment"),
        key: Long = IdentifierGenerator.generate("featureKey"),
        status: Experiment.Status,
        init: ExperimentDsl.() -> Unit
    ): Experiment {
        return ExperimentDsl(id, key, FEATURE_FLAG, "\$id", status, 1, 1, null, this).apply(init).build().also {
            _featureFlags[it.key] = it
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

    override fun getExperimentOrNull(experimentKey: Long): Experiment? = _experiments[experimentKey]
    override fun getFeatureFlagOrNull(featureKey: Long): Experiment? = _featureFlags[featureKey]
    override fun getEventTypeOrNull(eventTypeKey: String): EventType? = eventTypes[eventTypeKey]
    override fun getBucketOrNull(bucketId: Long): Bucket? = buckets[bucketId]
    override fun getSegmentOrNull(segmentKey: String): Segment? = segments[segmentKey]
    override fun getContainerOrNull(containerId: Long): Container? = containers[containerId]
    override fun getParameterConfigurationOrNull(parameterConfigurationId: Long): ParameterConfiguration? =
        parameterConfigurations[parameterConfigurationId]

    override fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameter? = null
    override fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessage? = null

    override fun register(bucket: Bucket) {
        buckets[bucket.id] = bucket
    }

    fun build(): Workspace {
        return this
    }
}


object Workspaces {

    fun create(
        id: Long = 1,
        environmentId: Long = 1,
        experiments: List<Experiment> = emptyList(),
        featureFlags: List<Experiment> = emptyList(),
        inAppMessages: List<InAppMessage> = emptyList(),
        eventTypes: List<EventType> = emptyList(),
        buckets: List<Bucket> = emptyList(),
        segments: List<Segment> = emptyList(),
        containers: List<Container> = emptyList(),
        parameterConfigurations: List<ParameterConfiguration> = emptyList(),
        remoteConfigParameters: List<RemoteConfigParameter> = emptyList(),
    ): Workspace {
        return MockWorkspace(
            id,
            environmentId,
            experiments,
            featureFlags,
            inAppMessages,
            eventTypes,
            buckets,
            segments,
            containers,
            parameterConfigurations,
            remoteConfigParameters
        )
    }

    private class MockWorkspace(
        override val id: Long,
        override val environmentId: Long,
        override val experiments: List<Experiment>,
        override val featureFlags: List<Experiment>,
        override val inAppMessages: List<InAppMessage>,
        private val eventTypes: List<EventType>,
        private val buckets: List<Bucket>,
        private val segments: List<Segment>,
        private val containers: List<Container>,
        private val parameterConfigurations: List<ParameterConfiguration>,
        private val remoteConfigParameters: List<RemoteConfigParameter>,
    ) : Workspace {

        override fun getExperimentOrNull(experimentKey: Long): Experiment? {
            return experiments.find { it.key == experimentKey }
        }

        override fun getFeatureFlagOrNull(featureKey: Long): Experiment? {
            return featureFlags.find { it.key == featureKey }
        }

        override fun getEventTypeOrNull(eventTypeKey: String): EventType? {
            return eventTypes.find { it.key == eventTypeKey }
        }

        override fun getBucketOrNull(bucketId: Long): Bucket? {
            return buckets.find { it.id == bucketId }
        }

        override fun getSegmentOrNull(segmentKey: String): Segment? {
            return segments.find { it.key == segmentKey }
        }

        override fun getContainerOrNull(containerId: Long): Container? {
            return containers.find { it.id == containerId }
        }

        override fun getParameterConfigurationOrNull(parameterConfigurationId: Long): ParameterConfiguration? {
            return parameterConfigurations.find { it.id == parameterConfigurationId }
        }

        override fun getRemoteConfigParameterOrNull(parameterKey: String): RemoteConfigParameter? {
            return remoteConfigParameters.find { it.key == parameterKey }
        }

        override fun getInAppMessageOrNull(inAppMessageKey: Long): InAppMessage? {
            return inAppMessages.find { it.key == inAppMessageKey }
        }
    }
}
