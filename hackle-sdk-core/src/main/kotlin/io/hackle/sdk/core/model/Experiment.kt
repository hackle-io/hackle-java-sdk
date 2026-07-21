package io.hackle.sdk.core.model

import io.hackle.sdk.common.HackleExperiment

interface Experiment : Entity, HackleExperiment {
    override val id: Long
    override val key: Long
    override val version: Int
    val status: Status
    val order: Long
    val type: Type
    val executionVersion: Int

    enum class Type {
        AB_TEST, FEATURE_FLAG
    }

    enum class Status {
        DRAFT, RUNNING, PAUSED, COMPLETED;

        companion object {
            private val STATUSES = mapOf(
                "READY" to DRAFT,
                "RUNNING" to RUNNING,
                "PAUSED" to PAUSED,
                "STOPPED" to COMPLETED
            )

            fun from(executionStatus: String): Status? {
                return STATUSES[executionStatus]
            }
        }
    }
}

abstract class AbstractExperiment : AbstractEntity(), Experiment {
    final override val serviceType: ServiceType
        get() = when (type) {
            Experiment.Type.AB_TEST -> ServiceType.AB_TEST
            Experiment.Type.FEATURE_FLAG -> ServiceType.FEATURE_FLAG
        }

    override fun toString(): String {
        return "Experiment(id=$id, key=$key, type=$type, version=$version, status=$status)"
    }
}
